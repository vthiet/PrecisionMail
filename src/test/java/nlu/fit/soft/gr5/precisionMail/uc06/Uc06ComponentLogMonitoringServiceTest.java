package nlu.fit.soft.gr5.precisionMail.uc06;

import nlu.fit.soft.gr5.precisionMail.service.impl.LogMonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uc06ComponentLogMonitoringServiceTest {
    @TempDir
    Path tempDir;

    private Path activeLog;
    private LogMonitoringServiceImpl service;

    @BeforeEach
    void setUp() {
        activeLog = tempDir.resolve("system.log");
        service = new LogMonitoringServiceImpl(activeLog);
    }

    @Test
    void readRecentLinesReturnsEmptyListWhenActiveLogDoesNotExist() throws IOException {
        assertEquals(List.of(), service.readRecentLines(1000));
    }

    @Test
    void readRecentLinesGroupsStacktraceSanitizesSecretsAndRetainsLastRecords() throws IOException {
        Files.writeString(activeLog, String.join(System.lineSeparator(),
                "[2026-06-07 10:00:00.000] [INFO] [main] [Auth:10] - Login password=secret sender@example.com",
                "[2026-06-07 10:00:01.000] [ERROR] [main] [MailService:42] - Send failed token=abc receiver@example.com",
                "java.io.IOException: connection closed",
                "\tat MailService.send(MailService.java:42)",
                "[2026-06-07 10:00:02.000] [WARN] [main] [Queue:7] - Retry scheduled"
        ), StandardCharsets.UTF_8);

        List<String> records = service.readRecentLines(2);

        assertAll(
                () -> assertEquals(2, records.size()),
                () -> assertTrue(records.getFirst().contains("[ERROR]")),
                () -> assertTrue(records.getFirst().contains("java.io.IOException")),
                () -> assertTrue(records.getFirst().contains("[PROTECTED_TOKEN]")),
                () -> assertTrue(records.getFirst().contains("rec***@example.com")),
                () -> assertTrue(records.get(1).contains("[WARN]")),
                () -> assertFalse(String.join("\n", records).contains("token=abc"))
        );
    }

    @Test
    void streamAndFilterLogsFiltersCompleteSanitizedRecordsByLevelAndKeyword() throws IOException {
        Files.writeString(activeLog, String.join(System.lineSeparator(),
                "[2026-06-07 10:00:00.000] [INFO] [main] [Auth:10] - Started",
                "[2026-06-07 10:00:01.000] [ERROR] [main] [MailService:42] - Send failed password=secret receiver@example.com",
                "java.net.SocketTimeoutException: timeout",
                "[2026-06-07 10:00:02.000] [WARN] [main] [Queue:7] - Retry scheduled"
        ), StandardCharsets.UTF_8);

        List<String> records = service.streamAndFilterLogs("ERROR", "timeout", 1000);

        assertAll(
                () -> assertEquals(1, records.size()),
                () -> assertTrue(records.getFirst().contains("[ERROR]")),
                () -> assertTrue(records.getFirst().contains("SocketTimeoutException")),
                () -> assertTrue(records.getFirst().contains("[PROTECTED_PASSWORD]")),
                () -> assertFalse(records.getFirst().contains("password=secret"))
        );
    }

    @Test
    void exportActiveLogsWritesZipCopyWithoutDeletingOriginalLog() throws IOException {
        Files.writeString(activeLog, "system log content", StandardCharsets.UTF_8);

        Path exported = service.exportActiveLogs(tempDir.resolve("technical-log"));

        assertAll(
                () -> assertEquals("technical-log.zip", exported.getFileName().toString()),
                () -> assertTrue(Files.exists(exported)),
                () -> assertTrue(Files.exists(activeLog)),
                () -> assertTrue(Files.size(exported) > 0)
        );
    }

    @Test
    void rejectsInvalidMaxLines() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.readRecentLines(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.streamAndFilterLogs("ALL", "", 0))
        );
    }
}
