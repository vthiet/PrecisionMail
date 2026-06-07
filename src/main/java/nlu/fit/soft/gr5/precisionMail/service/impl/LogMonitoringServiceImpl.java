package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.service.LogMonitoringService;
import nlu.fit.soft.gr5.precisionMail.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class LogMonitoringServiceImpl implements LogMonitoringService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogMonitoringServiceImpl.class);
    private static final long SAFE_UI_LOG_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Pattern LOG_RECORD_START = Pattern.compile(
            "^\\[[^]]+] \\[[^]]+] \\[[^]]+] \\[[^]]+] - .*$"
    );
    private static final Path ACTIVE_LOG = Path.of(
            System.getProperty("user.home"),
            ".precisionmail",
            "logs",
            "system.log"
    );

    @Override
    public List<String> readRecentLines(int maxLines) throws IOException {
        validateMaxLines(maxLines);
        if (!Files.exists(ACTIVE_LOG)) {
            // AF-6.1.3-A1: an absent initial log is a valid empty state; WatchService waits for creation.
            return List.of();
        }
        if (isActiveLogOversized()) {
            LOGGER.warn("Log file size [{}] MB exceeds safe load threshold of 10MB. Loading truncated view (last [{}] lines) to prevent OutOfMemoryError.",
                    Files.size(ACTIVE_LOG) / (1024 * 1024),
                    maxLines);
        }

        // BF-6.1.3-5 / EF-6.1.3-E2: retain only the latest complete records and sanitize before returning.
        ArrayDeque<String> buffer = new ArrayDeque<>(maxLines);
        try (var lines = Files.lines(ACTIVE_LOG)) {
            collectLogRecords(lines::forEach, record -> addBounded(buffer, LogSanitizer.sanitize(record), maxLines));
        }
        return new ArrayList<>(buffer);
    }

    @Override
    public List<String> streamAndFilterLogs(String level, String keyword, int maxLines) throws IOException {
        validateMaxLines(maxLines);
        if (!Files.exists(ACTIVE_LOG)) {
            return List.of();
        }

        String normalizedLevel = normalize(level);
        String normalizedKeyword = normalize(keyword);
        ArrayDeque<String> buffer = new ArrayDeque<>(maxLines);
        try (var lines = Files.lines(ACTIVE_LOG)) {
            // BF-6.1.8-9: filter complete sanitized records so a matching ERROR keeps its stacktrace.
            collectLogRecords(lines::forEach, record -> {
                String sanitized = LogSanitizer.sanitize(record);
                if (matchesLevel(sanitized, normalizedLevel)
                        && (normalizedKeyword.isBlank() || normalize(sanitized).contains(normalizedKeyword))) {
                    addBounded(buffer, sanitized, maxLines);
                }
            });
        }
        return new ArrayList<>(buffer);
    }

    @Override
    public Path exportActiveLogs(Path destination) throws IOException {
        // BF-6.1.18 / EF-6.1.18-E1: export a ZIP copy and never modify or remove the active log.
        if (!Files.exists(ACTIVE_LOG)) {
            throw new IOException("Active log file does not exist: " + ACTIVE_LOG);
        }

        Path target = destination;
        if (!target.toString().toLowerCase(Locale.ROOT).endsWith(".zip")) {
            target = target.resolveSibling(target.getFileName() + ".zip");
        }

        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(
                target,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE))) {
            zip.putNextEntry(new ZipEntry(ACTIVE_LOG.getFileName().toString()));
            Files.copy(ACTIVE_LOG, zip);
            zip.closeEntry();
        }
        return target;
    }

    @Override
    public LogWatchRegistration watchActiveLog(Consumer<List<String>> newLinesConsumer) throws IOException {
        // BF-6.1.7: monitor create/modify events on a virtual thread without blocking JavaFX.
        Files.createDirectories(activeLogDirectory());
        WatchService watchService = activeLogDirectory().getFileSystem().newWatchService();
        activeLogDirectory().register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
        AtomicBoolean running = new AtomicBoolean(true);
        Thread watcher = Thread.ofVirtual().name("precisionmail-log-watch").start(() -> watchLoop(watchService, running, newLinesConsumer));
        return () -> {
            running.set(false);
            watcher.interrupt();
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.debug("Failed to close log WatchService cleanly.", e);
            }
        };
    }

    @Override
    public Path activeLogFile() {
        return ACTIVE_LOG;
    }

    @Override
    public Path activeLogDirectory() {
        return ACTIVE_LOG.getParent();
    }

    @Override
    public boolean isActiveLogOversized() throws IOException {
        return Files.exists(ACTIVE_LOG) && Files.size(ACTIVE_LOG) > SAFE_UI_LOG_SIZE_BYTES;
    }

    private void watchLoop(WatchService watchService, AtomicBoolean running, Consumer<List<String>> newLinesConsumer) {
        long lastPosition = initialPosition();
        while (running.get()) {
            try {
                WatchKey key = watchService.take();
                boolean changed = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (ACTIVE_LOG.getFileName().equals(event.context())) {
                        changed = true;
                    }
                }
                if (changed) {
                    ReadTailResult result = readFrom(lastPosition);
                    lastPosition = result.position();
                    if (!result.lines().isEmpty()) {
                        newLinesConsumer.accept(result.lines());
                    }
                }
                if (!key.reset()) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                LOGGER.warn("Failed while watching active log file.", e);
            }
        }
    }

    private long initialPosition() {
        try {
            return Files.exists(ACTIVE_LOG) ? Files.size(ACTIVE_LOG) : 0L;
        } catch (IOException e) {
            return 0L;
        }
    }

    private ReadTailResult readFrom(long position) throws IOException {
        if (!Files.exists(ACTIVE_LOG)) {
            return new ReadTailResult(position, List.of());
        }
        long size = Files.size(ACTIVE_LOG);
        long start = position > size ? 0L : position;
        try (var channel = Files.newByteChannel(ACTIVE_LOG, StandardOpenOption.READ)) {
            channel.position(start);
            byte[] bytes = channelToBytes(channel, size - start);
            String text = new String(bytes, StandardCharsets.UTF_8);
            List<String> lines = text.lines()
                    .filter(line -> !line.isBlank())
                    .toList();
            List<String> records = groupLogRecords(lines).stream()
                    .map(LogSanitizer::sanitize)
                    .toList();
            return new ReadTailResult(size, records);
        }
    }

    private void collectLogRecords(Consumer<Consumer<String>> lineSource, Consumer<String> recordConsumer) {
        StringBuilder current = new StringBuilder();
        lineSource.accept(line -> {
            if (LOG_RECORD_START.matcher(line).matches() && !current.isEmpty()) {
                recordConsumer.accept(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append(System.lineSeparator());
            }
            current.append(line);
        });
        if (!current.isEmpty()) {
            recordConsumer.accept(current.toString());
        }
    }

    List<String> groupLogRecords(List<String> lines) {
        List<String> records = new ArrayList<>();
        collectLogRecords(lines::forEach, records::add);
        return records;
    }

    private void addBounded(ArrayDeque<String> buffer, String record, int maxLines) {
        if (buffer.size() == maxLines) {
            buffer.removeFirst();
        }
        buffer.addLast(record);
    }

    private void validateMaxLines(int maxLines) {
        if (maxLines < 1) {
            throw new IllegalArgumentException("maxLines must be greater than zero.");
        }
    }

    private byte[] channelToBytes(java.nio.channels.SeekableByteChannel channel, long expectedBytes) throws IOException {
        if (expectedBytes <= 0) {
            return new byte[0];
        }
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream((int) Math.min(expectedBytes, 8192));
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(8192);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            output.write(buffer.array(), 0, buffer.limit());
            buffer.clear();
        }
        return output.toByteArray();
    }

    private boolean matchesLevel(String line, String normalizedLevel) {
        return normalizedLevel.isBlank() || "ALL".equals(normalizedLevel) || normalize(line).contains("[" + normalizedLevel + "]");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private record ReadTailResult(long position, List<String> lines) {
    }
}
