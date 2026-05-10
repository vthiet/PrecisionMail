package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.MessagingException;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.ScheduledEmailService;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledEmailServiceImpl implements ScheduledEmailService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void schedule(ScheduledEmail scheduledEmail) {
        long deplay = Duration.between(LocalDateTime.now(), scheduledEmail.scheduledAt).toMillis();

        scheduler.schedule(() -> {
            try {
                EmailUtil.send(scheduledEmail.account, scheduledEmail.email);
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, deplay, TimeUnit.MILLISECONDS);
    }
}
