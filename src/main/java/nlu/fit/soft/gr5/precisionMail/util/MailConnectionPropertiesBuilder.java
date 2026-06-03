package nlu.fit.soft.gr5.precisionMail.util;

import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;

import java.util.Properties;

public final class MailConnectionPropertiesBuilder {
    private static final String DEFAULT_TIMEOUT_MILLIS = "10000";

    private MailConnectionPropertiesBuilder() {
    }

    public static Properties smtpProperties(MailServerConfig config) {
        Properties props = new Properties();
        putSmtpBaseProperties(props, config);
        applySmtpSecurity(props, config);
        return props;
    }

    public static Properties validationProperties(MailServerConfig config) {
        Properties props = smtpProperties(config);
        putImapBaseProperties(props, config);
        applyImapSecurity(props, config);
        return props;
    }

    private static void putSmtpBaseProperties(Properties props, MailServerConfig config) {
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", config.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
        props.put("mail.smtp.connectiontimeout", DEFAULT_TIMEOUT_MILLIS);
        props.put("mail.smtp.timeout", DEFAULT_TIMEOUT_MILLIS);
        props.put("mail.smtp.writetimeout", DEFAULT_TIMEOUT_MILLIS);
    }

    private static void putImapBaseProperties(Properties props, MailServerConfig config) {
        props.put("mail.imap.host", config.getImapHost());
        props.put("mail.imap.port", String.valueOf(config.getImapPort()));
        props.put("mail.imap.connectiontimeout", DEFAULT_TIMEOUT_MILLIS);
        props.put("mail.imap.timeout", DEFAULT_TIMEOUT_MILLIS);
        props.put("mail.imap.writetimeout", DEFAULT_TIMEOUT_MILLIS);
    }

    private static void applySmtpSecurity(Properties props, MailServerConfig config) {
        if (config.getSecurityMode() == SecurityMode.SSL) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
    }

    private static void applyImapSecurity(Properties props, MailServerConfig config) {
        if (config.getSecurityMode() == SecurityMode.SSL) {
            props.put("mail.imap.ssl.enable", "true");
            return;
        }

        props.put("mail.imap.starttls.enable", "true");
        if (config.getImapPort() == 993) {
            props.put("mail.imap.ssl.enable", "true");
        }
    }
}
