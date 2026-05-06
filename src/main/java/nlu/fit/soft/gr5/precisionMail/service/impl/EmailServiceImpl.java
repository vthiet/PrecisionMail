package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.util.AppLoaderUtil;

import java.util.List;
import java.util.Properties;

public class EmailServiceImpl implements EmailService {
    private static final String username = AppLoaderUtil.getProperty("mail.username");
    private static final String password = AppLoaderUtil.getProperty("mail.password");

    public EmailServiceImpl() { }

    @Override
    public void send(String to, String subject, String content) throws MessagingException {
        Session session = createSession();
        MimeMessage message = new MimeMessage(session);

        // email send
        message.setFrom(new InternetAddress(username));

        // email reception
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
    }

    @Override
    public void send(List<String> to,
                     List<String> cc,
                     List<String> bcc,
                     String subject,
                     String content) throws MessagingException {

        Session session = createSession();
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));

        // TO
        if (to != null && !to.isEmpty()) {
            message.setRecipients(
                    Message.RecipientType.TO,
                    parseAddresses(to)
            );
        }

        // CC
        if (cc != null && !cc.isEmpty()) {
            message.setRecipients(
                    Message.RecipientType.CC,
                    parseAddresses(cc)
            );
        }

        // BCC
        if (bcc != null && !bcc.isEmpty()) {
            message.setRecipients(
                    Message.RecipientType.BCC,
                    parseAddresses(bcc)
            );
        }

        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
    }

    private InternetAddress[] parseAddresses(List<String> emails) throws AddressException {
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

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}