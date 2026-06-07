package nlu.fit.soft.gr5.precisionMail.uc06;

import nlu.fit.soft.gr5.precisionMail.model.LogEntry;
import nlu.fit.soft.gr5.precisionMail.service.LogMonitoringService;
import nlu.fit.soft.gr5.precisionMail.service.impl.LogMonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uc06SystemLogMonitoringFlowTest {
    @TempDir
    Path tempDir;

    private Path activeLog;
    private LogMonitoringService service;

    @BeforeEach
    void setUp() {
        activeLog = tempDir.resolve("system.log");
        service = new LogMonitoringServiceImpl(activeLog);
    }

    @Test
    void loadParseMaskAndShowErrorDetailFlowKeepsSensitiveValuesOutOfUiRecords() throws IOException {
        Files.writeString(activeLog, String.join(System.lineSeparator(),
                "[2026-06-07 10:00:00.000] [INFO] [main] [Launcher:20] - Application started",
                "[2026-06-07 10:00:01.000] [ERROR] [mail-worker] [MailService:42] - Send failed password=secret token=abc recipient@example.com",
                "java.net.SocketTimeoutException: timeout",
                "\tat MailService.send(MailService.java:42)"
        ), StandardCharsets.UTF_8);

        List<LogEntry> entries = service.readRecentLines(1000).stream()
                .map(LogEntry::parse)
                .toList();
        LogEntry error = entries.stream()
                .filter(entry -> "ERROR".equals(entry.level()))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertEquals(2, entries.size()),
                () -> assertEquals("MailService:42", error.source()),
                () -> assertTrue(error.detailText().contains("SocketTimeoutException")),
                () -> assertTrue(error.detailText().contains("[PROTECTED_PASSWORD]")),
                () -> assertTrue(error.detailText().contains("[PROTECTED_TOKEN]")),
                () -> assertTrue(error.detailText().contains("rec***@example.com")),
                () -> assertFalse(error.detailText().contains("password=secret")),
                () -> assertFalse(error.detailText().contains("token=abc"))
        );
    }

    @Test
    void filterThenExportFlowReturnsMatchingRecordsAndZipContainsActiveLog() throws IOException {
        Files.writeString(activeLog, String.join(System.lineSeparator(),
                "[2026-06-07 10:00:00.000] [INFO] [main] [Launcher:20] - Application started",
                "[2026-06-07 10:00:01.000] [WARN] [queue-worker] [Queue:88] - Retry scheduled for customer@example.com",
                "[2026-06-07 10:00:02.000] [ERROR] [mail-worker] [MailService:42] - SMTP failed"
        ), StandardCharsets.UTF_8);

        List<LogEntry> filtered = service.streamAndFilterLogs("WARN", "retry", 1000).stream()
                .map(LogEntry::parse)
                .toList();
        Path exported = service.exportActiveLogs(tempDir.resolve("support-log.zip"));

        assertAll(
                () -> assertEquals(1, filtered.size()),
                () -> assertEquals("WARN", filtered.getFirst().level()),
                () -> assertTrue(filtered.getFirst().rawLine().contains("cus***@example.com")),
                () -> assertTrue(Files.exists(exported))
        );

        try (ZipFile zipFile = new ZipFile(exported.toFile())) {
            assertNotNull(zipFile.getEntry("system.log"));
        }
    }
}
