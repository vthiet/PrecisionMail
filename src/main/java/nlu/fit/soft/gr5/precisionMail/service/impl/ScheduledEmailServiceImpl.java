package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.MessagingException;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.ScheduledEmailService;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledEmailServiceImpl implements ScheduledEmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledEmailServiceImpl.class);
    private final EmailDao emailDao = new EmailDaoImpl();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "scheduled-email-worker");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void schedule(ScheduledEmail scheduledEmail) {
        long delay = Duration.between(LocalDateTime.now(), scheduledEmail.scheduledAt).toMillis();

        if (delay < 0) {
            LOGGER.warn(
                    "Rejected scheduled email in the past. sender={}, scheduledAt={}.",
                    LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                    scheduledEmail.scheduledAt
            );
            throw new IllegalArgumentException("Scheduled time must be in the future.");
        }

        LOGGER.info(
                "Scheduled email created. sender={}, scheduledAt={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                scheduledEmail.scheduledAt,
                LogHelper.recipientCount(scheduledEmail.email),
                LogHelper.attachmentCount(scheduledEmail.email)
        );

        scheduler.schedule(() -> {
            try {
                LOGGER.info(
                        "Scheduled email dispatch started. sender={}, scheduledAt={}.",
                        LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                        scheduledEmail.scheduledAt
                );
                EmailUtil.send(scheduledEmail.account, scheduledEmail.email);
                emailDao.save(scheduledEmail.email);
                LOGGER.info(
                        "Scheduled email sent successfully. sender={}, scheduledAt={}.",
                        LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                        scheduledEmail.scheduledAt
                );
            } catch (MessagingException | IOException | RuntimeException e) {
                LOGGER.error(
                        "Scheduled email send failed. sender={}, scheduledAt={}.",
                        LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                        scheduledEmail.scheduledAt,
                        e
                );
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
