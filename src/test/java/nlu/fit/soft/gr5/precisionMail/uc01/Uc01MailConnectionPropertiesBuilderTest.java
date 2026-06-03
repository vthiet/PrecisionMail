package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import nlu.fit.soft.gr5.precisionMail.util.MailConnectionPropertiesBuilder;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Uc01MailConnectionPropertiesBuilderTest {
    private static final String TIMEOUT = "10000";

    @Test
    void buildsSmtpPropertiesForTls() {
        MailServerConfig config = new MailServerConfig(
                "smtp.gmail.com",
                587,
                "imap.gmail.com",
                993,
                SecurityMode.TLS
        );

        Properties props = MailConnectionPropertiesBuilder.smtpProperties(config);

        assertAll(
                () -> assertEquals("true", props.getProperty("mail.smtp.auth")),
                () -> assertEquals("smtp.gmail.com", props.getProperty("mail.smtp.host")),
                () -> assertEquals("587", props.getProperty("mail.smtp.port")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.smtp.connectiontimeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.smtp.timeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.smtp.writetimeout")),
                () -> assertEquals("true", props.getProperty("mail.smtp.starttls.enable")),
                () -> assertNull(props.getProperty("mail.smtp.ssl.enable"))
        );
    }

    @Test
    void buildsValidationPropertiesForTls() {
        MailServerConfig config = new MailServerConfig(
                "smtp.gmail.com",
                587,
                "imap.gmail.com",
                993,
                SecurityMode.TLS
        );

        Properties props = MailConnectionPropertiesBuilder.validationProperties(config);

        assertAll(
                () -> assertEquals("true", props.getProperty("mail.smtp.auth")),
                () -> assertEquals("smtp.gmail.com", props.getProperty("mail.smtp.host")),
                () -> assertEquals("587", props.getProperty("mail.smtp.port")),
                () -> assertEquals("true", props.getProperty("mail.smtp.starttls.enable")),
                () -> assertNull(props.getProperty("mail.smtp.ssl.enable")),
                () -> assertEquals("imap.gmail.com", props.getProperty("mail.imap.host")),
                () -> assertEquals("993", props.getProperty("mail.imap.port")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.imap.connectiontimeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.imap.timeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.imap.writetimeout")),
                () -> assertEquals("true", props.getProperty("mail.imap.starttls.enable")),
                () -> assertEquals("true", props.getProperty("mail.imap.ssl.enable"))
        );
    }

    @Test
    void buildsValidationPropertiesForSsl() {
        MailServerConfig config = new MailServerConfig(
                "smtp.mail.yahoo.com",
                465,
                "imap.mail.yahoo.com",
                993,
                SecurityMode.SSL
        );

        Properties props = MailConnectionPropertiesBuilder.validationProperties(config);

        assertAll(
                () -> assertEquals("true", props.getProperty("mail.smtp.auth")),
                () -> assertEquals("smtp.mail.yahoo.com", props.getProperty("mail.smtp.host")),
                () -> assertEquals("465", props.getProperty("mail.smtp.port")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.smtp.connectiontimeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.smtp.timeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.smtp.writetimeout")),
                () -> assertEquals("true", props.getProperty("mail.smtp.ssl.enable")),
                () -> assertNull(props.getProperty("mail.smtp.starttls.enable")),
                () -> assertEquals("imap.mail.yahoo.com", props.getProperty("mail.imap.host")),
                () -> assertEquals("993", props.getProperty("mail.imap.port")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.imap.connectiontimeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.imap.timeout")),
                () -> assertEquals(TIMEOUT, props.getProperty("mail.imap.writetimeout")),
                () -> assertEquals("true", props.getProperty("mail.imap.ssl.enable")),
                () -> assertNull(props.getProperty("mail.imap.starttls.enable"))
        );
    }
}
