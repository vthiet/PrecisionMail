package nlu.fit.soft.gr5.precisionMail.uc06;

import nlu.fit.soft.gr5.precisionMail.model.LogEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEntryTest {
    @Test
    void parsesAndDisplaysGroupedMultilineStacktrace() {
        String raw = "[2026-06-07 10:00:00.000] [ERROR] [main] [MailService:42] - Send failed"
                + System.lineSeparator()
                + "java.io.IOException: connection closed";

        LogEntry entry = LogEntry.parse(raw);

        assertEquals("ERROR", entry.level());
        assertEquals(raw, entry.detailText());
    }

    @Test
    void explainsWhenErrorHasNoStacktrace() {
        LogEntry entry = LogEntry.parse(
                "[2026-06-07 10:00:00.000] [ERROR] [main] [MailService:42] - Send failed"
        );

        assertTrue(entry.detailText().contains("Không có Stacktrace chi tiết"));
    }
}
