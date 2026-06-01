package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.MailServerConfigValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uc01ValidationHelperTest {

    @Test
    void acceptsValidEmailAddresses() {
        assertAll(
                () -> assertTrue(EmailUtil.isValidEmail("sender@gmail.com")),
                () -> assertTrue(EmailUtil.isValidEmail("first.last+tag@outlook.com")),
                () -> assertTrue(EmailUtil.isValidEmail("user_01@mail.example.vn"))
        );
    }

    @Test
    void rejectsInvalidEmailAddresses() {
        assertAll(
                () -> assertFalse(EmailUtil.isValidEmail(null)),
                () -> assertFalse(EmailUtil.isValidEmail("")),
                () -> assertFalse(EmailUtil.isValidEmail("abc")),
                () -> assertFalse(EmailUtil.isValidEmail("sender@")),
                () -> assertFalse(EmailUtil.isValidEmail("@gmail.com")),
                () -> assertFalse(EmailUtil.isValidEmail("sender gmail.com"))
        );
    }

    @Test
    void acceptsPortBoundaryValues() {
        assertAll(
                () -> assertTrue(MailServerConfigValidator.isValidPort("1")),
                () -> assertTrue(MailServerConfigValidator.isValidPort("65535")),
                () -> assertTrue(MailServerConfigValidator.isValidPort(" 587 "))
        );
    }

    @Test
    void rejectsPortsOutsideAllowedRangeAndNonNumericInput() {
        assertAll(
                () -> assertFalse(MailServerConfigValidator.isValidPort("0")),
                () -> assertFalse(MailServerConfigValidator.isValidPort("99999")),
                () -> assertFalse(MailServerConfigValidator.isValidPort("abc")),
                () -> assertFalse(MailServerConfigValidator.isValidPort("")),
                () -> assertFalse(MailServerConfigValidator.isValidPort(null))
        );
    }

    @Test
    void parsePortReturnsInvalidSentinelForNonNumericInput() {
        assertAll(
                () -> assertEquals(587, MailServerConfigValidator.parsePort("587")),
                () -> assertEquals(-1, MailServerConfigValidator.parsePort("abc")),
                () -> assertEquals(-1, MailServerConfigValidator.parsePort(null))
        );
    }
}
