package nlu.fit.soft.gr5.precisionMail.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.ConnectionTestResult;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmailUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtil.class);
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_EXTRACT_PATTERN =
            Pattern.compile("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+");

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static Set<String> emailFeature(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(plainText.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean containsOnlyValidEmails(String plainText) {
        Set<String> emails = emailFeature(plainText);
        if (emails.isEmpty()) {
            return true;
        }

        return emails.stream().allMatch(EmailUtil::isValidEmail);
    }

    public static boolean hasAnyValidEmail(String plainText) {
        return emailFeature(plainText).stream().anyMatch(EmailUtil::isValidEmail);
    }

    public static Set<String> extractEmails(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return Set.of();
        }

        LinkedHashSet<String> emails = new LinkedHashSet<>();
        Matcher matcher = EMAIL_EXTRACT_PATTERN.matcher(rawText);
        while (matcher.find()) {
            String email = matcher.group().trim();
            if (isValidEmail(email)) {
                emails.add(email);
            }
        }
        return emails;
    }

    public static InternetAddress[] parseAddresses(Set<String> emails) {
        return emails.stream()
                .map(email -> {
                    try {
                        InternetAddress addr = new InternetAddress(email.trim());
                        addr.validate();
                        return addr;
                    } catch (AddressException e) {
                        throw new IllegalArgumentException("Invalid email: " + email, e);
                    }
                })
                .toArray(InternetAddress[]::new);
    }

    public static Session getSession(Account account) {
        Properties props = MailConnectionPropertiesBuilder.smtpProperties(account.getMailServerConfig());

        LOGGER.debug("Creating SMTP session for sender={}.", LogHelper.maskEmail(account.getUsername()));

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account.getUsername(), account.getPassword());
            }
        });
    }

    public static ConnectionTestResult validateConnection(Account account) {
        MailServerConfig config = account.getMailServerConfig();
        Properties props = MailConnectionPropertiesBuilder.validationProperties(config);
        Session session = Session.getInstance(props);
        LOGGER.info("Testing mail server connection for sender={}.", LogHelper.maskEmail(account.getUsername()));

        try (Transport transport = session.getTransport("smtp")) {
            transport.connect(
                    config.getSmtpHost(),
                    config.getSmtpPort(),
                    account.getUsername(),
                    account.getPassword()
            );
        } catch (MessagingException ex) {
            return connectionFailure(ConnectionTestResult.Type.SMTP_FAILED, "SMTP connection test failed.", ex);
        }

        try (Store store = session.getStore("imap")) {
            store.connect(
                    config.getImapHost(),
                    config.getImapPort(),
                    account.getUsername(),
                    account.getPassword()
            );
        } catch (MessagingException ex) {
            return connectionFailure(ConnectionTestResult.Type.IMAP_FAILED, "IMAP connection test failed.", ex);
        }

        return ConnectionTestResult.success();
    }

    private static ConnectionTestResult connectionFailure(
            ConnectionTestResult.Type protocolFailureType,
            String message,
            MessagingException ex
    ) {
        if (hasCause(ex, AuthenticationFailedException.class)) {
            return ConnectionTestResult.failure(ConnectionTestResult.Type.AUTH_FAILED, "Mail server authentication failed.", ex);
        }
        if (hasCause(ex, SocketTimeoutException.class)
                || hasCause(ex, ConnectException.class)
                || hasCause(ex, UnknownHostException.class)
                || hasCause(ex, NoRouteToHostException.class)) {
            return ConnectionTestResult.failure(ConnectionTestResult.Type.TIMEOUT, "Mail server connection timed out or failed.", ex);
        }
        return ConnectionTestResult.failure(protocolFailureType, message, ex);
    }

    private static boolean hasCause(Throwable throwable, Class<? extends Throwable> expectedType) {
        Throwable current = throwable;
        while (current != null) {
            if (expectedType.isInstance(current)) {
                return true;
            }
            if (current instanceof MessagingException messagingException && messagingException.getNextException() != null) {
                current = messagingException.getNextException();
            } else {
                current = current.getCause();
            }
        }
        return false;
    }

    public static void send(Account account, Email email) throws MessagingException, IOException {
        Set<String> toList = email.toLst;
        Set<String> ccList = email.cc;
        Set<String> bccList = email.bcc;

        Session session = getSession(account);
        MimeMessage message = new MimeMessage(session);

        if (toList != null && !toList.isEmpty()) {
            message.setRecipients(Message.RecipientType.TO, parseAddresses(toList));
        }

        if (ccList != null && !ccList.isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, parseAddresses(ccList));
        }

        if (bccList != null && !bccList.isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, parseAddresses(bccList));
        }

        message.setFrom(new InternetAddress(account.getUsername()));
        message.setSubject(email.subject);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(email.content == null ? "" : email.content, "text/html; charset=UTF-8");
        multipart.addBodyPart(textPart);

        if (email.attachments != null) {
            for (String attachFilePath : email.attachments) {
                File file = new File(attachFilePath);

                if (!file.exists()) {
                    LOGGER.warn("Attachment skipped because file does not exist.");
                    continue;
                }

                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                // Tên file tiếng Việt
                attachmentPart.setFileName(MimeUtility.encodeText(file.getName()));

                multipart.addBodyPart(attachmentPart);
            }
        }

        message.setContent(multipart);
        message.setSentDate(new Date());

        Transport.send(message);
        LOGGER.debug("SMTP transport completed successfully for sender={}.", LogHelper.maskEmail(account.getUsername()));
    }
}
