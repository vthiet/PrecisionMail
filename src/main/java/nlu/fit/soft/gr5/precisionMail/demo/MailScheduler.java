package nlu.fit.soft.gr5.precisionMail.demo;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.concurrent.*;

public class MailScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void scheduleEmail(String to, String subject, String body, long delayInSeconds) {
        Runnable task = () -> {
            try {
                Message message = new MimeMessage(MailConfig.getSession());
                message.setFrom(new InternetAddress("your-email@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                System.out.println("Email đã được gửi tới: " + to);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        };

        scheduler.schedule(task, delayInSeconds, TimeUnit.SECONDS);
        System.out.println("Đã hẹn giờ gửi mail sau " + delayInSeconds + " giây.");
    }
}