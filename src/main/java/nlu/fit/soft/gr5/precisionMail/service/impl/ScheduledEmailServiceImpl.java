package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.MessagingException;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.ScheduledEmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.dao.impl.ScheduledEmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.ScheduledEmailService;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledEmailServiceImpl implements ScheduledEmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledEmailServiceImpl.class);
    private static final long MINIMUM_LEAD_TIME_SECONDS = 60;
    private final EmailDao emailDao = new EmailDaoImpl();
    private final ScheduledEmailDao scheduledEmailDao = new ScheduledEmailDaoImpl();

    @Override
    public void schedule(ScheduledEmail scheduledEmail) {
        scheduleJob(scheduledEmail);
    }

    public ScheduledFuture<?> scheduleJob(ScheduledEmail scheduledEmail) {
        long delay = Duration.between(LocalDateTime.now(), scheduledEmail.scheduledAt).toMillis();

        if (delay < Duration.ofSeconds(MINIMUM_LEAD_TIME_SECONDS).toMillis()) {
            LOGGER.warn(
                    "Rejected scheduled email because lead time is too short. sender={}, scheduledAt={}.",
                    LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                    scheduledEmail.scheduledAt
            );
            throw new IllegalArgumentException("Scheduled time must be at least 60 seconds in the future.");
        }

        try {
            scheduledEmail.status = EmailStatus.SCHEDULED;
            scheduledEmailDao.save(scheduledEmail);
        } catch (IOException e) {
            throw new RuntimeException("Cannot persist scheduled email.", e);
        }

        LOGGER.info(
                "Scheduled email created. sender={}, scheduledAt={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                scheduledEmail.scheduledAt,
                LogHelper.recipientCount(scheduledEmail.email),
                LogHelper.attachmentCount(scheduledEmail.email)
        );

        return AppExecutors.scheduler().schedule(() -> {
            try {
                LOGGER.info(
                        "Scheduled email dispatch started. sender={}, scheduledAt={}.",
                        LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                        scheduledEmail.scheduledAt
                );
                scheduledEmail.status = EmailStatus.SENDING;
                scheduledEmailDao.updateStatus(scheduledEmail.id, EmailStatus.SENDING, null);
                EmailUtil.send(scheduledEmail.account, scheduledEmail.email);
                scheduledEmail.email.status = EmailStatus.SENT;
                scheduledEmail.email.sentAt = LocalDateTime.now();
                emailDao.save(scheduledEmail.email);
                scheduledEmailDao.updateStatus(scheduledEmail.id, EmailStatus.SENT, null);
                LOGGER.info(
                        "Scheduled email sent successfully. sender={}, scheduledAt={}.",
                        LogHelper.maskEmail(scheduledEmail.account.getUsername()),
                        scheduledEmail.scheduledAt
                );
            } catch (MessagingException | IOException | RuntimeException e) {
                try {
                    scheduledEmailDao.updateStatus(scheduledEmail.id, EmailStatus.RETRY_PENDING, e.getMessage());
                } catch (IOException saveException) {
                    LOGGER.error("Failed to persist scheduled email failure.", saveException);
                }
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
