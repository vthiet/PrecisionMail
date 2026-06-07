package nlu.fit.soft.gr5.precisionMail.util;

import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;

import java.util.Properties;

/**
 * Builder tạo {@link Properties} cho kết nối SMTP/IMAP của UC-01.
 *
 * <p>Commit UC-01 #6-#7 - Anh Han: tách phần cấu hình JavaMail khỏi
 * {@link EmailUtil} để dễ kiểm thử timeout, host, port và SSL/TLS.</p>
 *
 * @author Anh Han
 */
public final class MailConnectionPropertiesBuilder {
    private static final String DEFAULT_TIMEOUT_MILLIS = "10000";

    private MailConnectionPropertiesBuilder() {
    }

    /**
     * Tạo properties dùng cho gửi mail qua SMTP.
     *
     * @param config cấu hình mail server của tài khoản
     * @return properties cho SMTP session
     */
    public static Properties smtpProperties(MailServerConfig config) {
        Properties props = new Properties();
        putSmtpBaseProperties(props, config);
        applySmtpSecurity(props, config);
        return props;
    }

    /**
     * Tạo properties dùng khi kiểm tra kết nối cả SMTP và IMAP.
     *
     * @param config cấu hình mail server của tài khoản
     * @return properties gồm timeout và security flag cho SMTP/IMAP
     */
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
        if (config.getSmtpSecurityMode() == SecurityMode.SSL) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
    }

    private static void applyImapSecurity(Properties props, MailServerConfig config) {
        if (config.getImapSecurityMode() == SecurityMode.SSL) {
            props.put("mail.imap.ssl.enable", "true");
        } else {
            props.put("mail.imap.starttls.enable", "true");
        }
    }
}
