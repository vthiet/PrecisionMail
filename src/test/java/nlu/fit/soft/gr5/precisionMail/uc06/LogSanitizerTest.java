package nlu.fit.soft.gr5.precisionMail.uc06;

import nlu.fit.soft.gr5.precisionMail.util.LogSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogSanitizerTest {
    @Test
    void masksPasswordTokenAndEmailWithTheirSpecifiedMarkers() {
        String value = "password=secret token=abc123 recipient@example.com";

        assertEquals(
                "password=[PROTECTED_PASSWORD] token=[PROTECTED_TOKEN] rec***@example.com",
                LogSanitizer.sanitize(value)
        );
    }
}
