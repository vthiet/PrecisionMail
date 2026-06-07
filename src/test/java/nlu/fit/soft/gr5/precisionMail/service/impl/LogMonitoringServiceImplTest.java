package nlu.fit.soft.gr5.precisionMail.service.impl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogMonitoringServiceImplTest {
    @Test
    void groupsStacktraceContinuationLinesWithTheirErrorRecord() {
        LogMonitoringServiceImpl service = new LogMonitoringServiceImpl();
        List<String> records = service.groupLogRecords(List.of(
                "[2026-06-07 10:00:00.000] [ERROR] [main] [MailService:42] - Send failed",
                "java.io.IOException: connection closed",
                "\tat MailService.send(MailService.java:42)",
                "[2026-06-07 10:00:01.000] [INFO] [main] [MailService:50] - Retry scheduled"
        ));

        assertEquals(2, records.size());
        assertTrue(records.getFirst().contains("java.io.IOException: connection closed"));
        assertTrue(records.getFirst().contains("MailService.java:42"));
    }
}
