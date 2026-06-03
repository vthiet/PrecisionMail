package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Uc01LogHelperTest {

    @Test
    void masksRegularEmailAddress() {
        assertEquals("sen***@gmail.com", LogHelper.maskEmail("sender@gmail.com"));
    }

    @Test
    void masksShortLocalPartWithoutExposingFullEmail() {
        assertAll(
                () -> assertEquals("ab***@gmail.com", LogHelper.maskEmail("ab@gmail.com")),
                () -> assertEquals("***", LogHelper.maskEmail("a@gmail.com")),
                () -> assertEquals("***", LogHelper.maskEmail("@gmail.com"))
        );
    }

    @Test
    void returnsUnknownForNullOrBlankEmail() {
        assertAll(
                () -> assertEquals("unknown", LogHelper.maskEmail(null)),
                () -> assertEquals("unknown", LogHelper.maskEmail("")),
                () -> assertEquals("unknown", LogHelper.maskEmail("   "))
        );
    }
}
