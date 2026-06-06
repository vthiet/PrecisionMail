package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.model.MailProviderPreset;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class Uc01MailProviderPresetTest {

    @Test
    void gmailPresetProvidesStandardSmtpAndImapConfiguration() {
        MailServerConfig config = MailProviderPreset.GMAIL.getConfig();

        assertAll(
                () -> assertEquals("smtp.gmail.com", config.getSmtpHost()),
                () -> assertEquals(587, config.getSmtpPort()),
                () -> assertEquals("imap.gmail.com", config.getImapHost()),
                () -> assertEquals(993, config.getImapPort()),
                () -> assertEquals(SecurityMode.TLS, config.getSmtpSecurityMode()),
                () -> assertEquals(SecurityMode.SSL, config.getImapSecurityMode())
        );
    }

    @Test
    void outlookPresetProvidesStandardSmtpAndImapConfiguration() {
        MailServerConfig config = MailProviderPreset.OUTLOOK.getConfig();

        assertAll(
                () -> assertEquals("smtp.office365.com", config.getSmtpHost()),
                () -> assertEquals(587, config.getSmtpPort()),
                () -> assertEquals("outlook.office365.com", config.getImapHost()),
                () -> assertEquals(993, config.getImapPort()),
                () -> assertEquals(SecurityMode.TLS, config.getSmtpSecurityMode()),
                () -> assertEquals(SecurityMode.SSL, config.getImapSecurityMode())
        );
    }

    @Test
    void yahooPresetProvidesStandardSmtpAndImapConfiguration() {
        MailServerConfig config = MailProviderPreset.YAHOO.getConfig();

        assertAll(
                () -> assertEquals("smtp.mail.yahoo.com", config.getSmtpHost()),
                () -> assertEquals(465, config.getSmtpPort()),
                () -> assertEquals("imap.mail.yahoo.com", config.getImapHost()),
                () -> assertEquals(993, config.getImapPort()),
                () -> assertEquals(SecurityMode.SSL, config.getSmtpSecurityMode()),
                () -> assertEquals(SecurityMode.SSL, config.getImapSecurityMode())
        );
    }

    @Test
    void infersPresetFromSavedConfiguration() {
        MailServerConfig outlook = new MailServerConfig(
                "SMTP.OFFICE365.COM",
                587,
                "OUTLOOK.OFFICE365.COM",
                993,
                SecurityMode.TLS,
                SecurityMode.SSL
        );

        MailServerConfig custom = new MailServerConfig(
                "smtp.example.com",
                2525,
                "imap.example.com",
                143,
                SecurityMode.TLS
        );

        assertAll(
                () -> assertEquals(MailProviderPreset.OUTLOOK, MailProviderPreset.inferFrom(outlook)),
                () -> assertEquals(MailProviderPreset.CUSTOM, MailProviderPreset.inferFrom(custom)),
                () -> assertEquals(MailProviderPreset.CUSTOM, MailProviderPreset.inferFrom(null))
        );
    }

    @Test
    void customProviderDoesNotProvidePresetConfiguration() {
        assertAll(
                () -> assertFalse(MailProviderPreset.CUSTOM.hasConfig()),
                () -> assertNull(MailProviderPreset.CUSTOM.getConfig()),
                () -> assertEquals("Custom", MailProviderPreset.CUSTOM.toString())
        );
    }
}
