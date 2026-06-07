package nlu.fit.soft.gr5.precisionMail.uc06;

import nlu.fit.soft.gr5.precisionMail.util.LogSanitizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Uc06LogSanitizerUnitTest {

    @Test
    void masksPasswordTokenSecretAndEmailValues() {
        String raw = "appPassword=\"abcd\" api_key=xyz secret=s1 recipient@example.com";

        assertEquals(
                "appPassword=[PROTECTED_PASSWORD] api_key=[PROTECTED_TOKEN] secret=[PROTECTED_PASSWORD] rec***@example.com",
                LogSanitizer.sanitize(raw)
        );
    }

    @Test
    void preservesNullAndBlankInput() {
        assertAll(
                () -> assertNull(LogSanitizer.sanitize(null)),
                () -> assertEquals("", LogSanitizer.sanitize("")),
                () -> assertEquals("   ", LogSanitizer.sanitize("   "))
        );
    }
}
