package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;

import java.util.Properties;

public class EmailServiceImpl implements EmailService {
    private final String username = "thietvo02@gmail.com";
    private final String password = "zykt ffvu jbvy vhzy";

    public EmailServiceImpl(){}

    @Override
    public void send(String to, String subject, String content) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);

        // email send
        message.setFrom(new InternetAddress(username));
        // email receipt
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
    }


}