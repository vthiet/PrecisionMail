package nlu.fit.soft.gr5.precisionMail.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    public static String getUsername(){
        return AppLoaderUtil.getProperty("mail.username");
    }

    public static String getPassword(){
        return AppLoaderUtil.getProperty("mail.password");
    }

    public static void sendEmail(String to, String subject, String content) {
        final String from = getUsername();
        final String password = getPassword();

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            System.out.println("Gửi email thành công!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
