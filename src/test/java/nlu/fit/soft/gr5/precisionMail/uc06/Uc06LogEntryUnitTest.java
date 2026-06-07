package nlu.fit.soft.gr5.precisionMail.uc06;

import nlu.fit.soft.gr5.precisionMail.model.LogEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uc06LogEntryUnitTest {

    @Test
    void parsesStandardLogRecordIntoDisplayFields() {
        String raw = "[2026-06-07 10:00:00.000] [WARN] [mail-worker] [MailService:42] - Retry scheduled";

        LogEntry entry = LogEntry.parse(raw);

        assertAll(
                () -> assertEquals("2026-06-07 10:00:00.000", entry.timestamp()),
                () -> assertEquals("WARN", entry.level()),
                () -> assertEquals("mail-worker", entry.thread()),
                () -> assertEquals("MailService:42", entry.source()),
                () -> assertEquals("Retry scheduled", entry.message()),
                () -> assertEquals(raw, entry.detailText())
        );
    }

    @Test
    void treatsMalformedLineAsRawMessageInsteadOfThrowing() {
        LogEntry entry = LogEntry.parse("malformed log line");

        assertAll(
                () -> assertEquals("", entry.timestamp()),
                () -> assertEquals("", entry.level()),
                () -> assertEquals("malformed log line", entry.message()),
                () -> assertEquals("malformed log line", entry.rawLine())
        );
    }

    @Test
    void errorWithoutGroupedStacktraceExplainsMissingDetail() {
        LogEntry entry = LogEntry.parse(
                "[2026-06-07 10:00:00.000] [ERROR] [main] [MailService:42] - Send failed"
        );

        assertTrue(entry.detailText().contains("Không có Stacktrace chi tiết"));
    }
}
