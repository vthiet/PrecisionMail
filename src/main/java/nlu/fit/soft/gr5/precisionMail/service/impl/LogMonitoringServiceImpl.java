package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.service.LogMonitoringService;
import nlu.fit.soft.gr5.precisionMail.util.LogSanitizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class LogMonitoringServiceImpl implements LogMonitoringService {
    private static final Path ACTIVE_LOG = Path.of(
            System.getProperty("user.home"),
            ".precisionmail",
            "logs",
            "system.log"
    );

    @Override
    public List<String> readRecentLines(int maxLines) throws IOException {
        if (!Files.exists(ACTIVE_LOG)) {
            return List.of();
        }

        ArrayDeque<String> buffer = new ArrayDeque<>(Math.max(1, maxLines));
        try (var lines = Files.lines(ACTIVE_LOG)) {
            lines.map(LogSanitizer::sanitize).forEach(line -> {
                if (buffer.size() == maxLines) {
                    buffer.removeFirst();
                }
                buffer.addLast(line);
            });
        }
        return new ArrayList<>(buffer);
    }

    @Override
    public Path activeLogFile() {
        return ACTIVE_LOG;
    }
}
