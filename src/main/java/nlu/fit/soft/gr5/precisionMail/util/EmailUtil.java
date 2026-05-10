package nlu.fit.soft.gr5.precisionMail.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class EmailUtil {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static Set<String> emailFeature(String plainText) {
        return Arrays.stream(plainText.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
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
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account.getUsername(), account.getPassword());
            }
        });
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
        textPart.setText(email.content); // setContent()
        multipart.addBodyPart(textPart);

        if (email.attachments != null) {
            for (String attachFilePath : email.attachments) {
                File file = new File(attachFilePath);

                if (!file.exists()) {
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
    }
}
