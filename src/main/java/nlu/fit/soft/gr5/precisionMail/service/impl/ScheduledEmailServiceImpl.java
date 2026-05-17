package nlu.fit.soft.gr5.precisionMail.service.impl;

import jakarta.mail.MessagingException;
import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.ScheduledEmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.EmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.dao.impl.ScheduledEmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;
import nlu.fit.soft.gr5.precisionMail.service.ScheduledEmailService;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledEmailServiceImpl implements ScheduledEmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledEmailServiceImpl.class);
    private static final long MINIMUM_LEAD_TIME_SECONDS = 60;
    private static final int MAX_NETWORK_RETRIES = 3;
    private static final long RETRY_DELAY_SECONDS = 300;

    private static final ScheduledEmailServiceImpl INSTANCE = new ScheduledEmailServiceImpl();

    private final EmailDao emailDao = new EmailDaoImpl();
    private final ScheduledEmailDao scheduledEmailDao = new ScheduledEmailDaoImpl();
    private final AccountService accountService = new AccountServiceImpl();
    private final Map<Long, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    public static ScheduledEmailServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public ScheduledEmail schedule(ScheduledEmail scheduledEmail) {
        validateLeadTime(scheduledEmail.scheduledAt);

        try {
            scheduledEmail.status = EmailStatus.SCHEDULED;
            scheduledEmail.retryCount = 0;
            ScheduledEmail saved = scheduledEmailDao.save(scheduledEmail);
            register(saved, Duration.between(LocalDateTime.now(), saved.scheduledAt));
            LOGGER.info(
                    "Scheduled email created. taskId={}, sender={}, scheduledAt={}, recipients={}, attachments={}.",
                    saved.id,
                    LogHelper.maskEmail(saved.account.getUsername()),
                    saved.scheduledAt,
                    LogHelper.recipientCount(saved.email),
                    LogHelper.attachmentCount(saved.email)
            );
            return saved;
        } catch (IOException e) {
            throw new RuntimeException("Cannot persist scheduled email.", e);
        }
    }

    @Override
    public void cancel(Long scheduledEmailId) {
        try {
            scheduledEmailDao.findById(scheduledEmailId).ifPresent(scheduledEmail -> {
                validateCanModify(scheduledEmail);
                try {
                    scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.CANCELLED, null);
                    ScheduledFuture<?> future = activeTasks.remove(scheduledEmailId);
                    if (future != null) {
                        future.cancel(false);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            LOGGER.info("Scheduled email cancelled. taskId={}.", scheduledEmailId);
        } catch (IOException e) {
            throw new RuntimeException("Cannot cancel scheduled email.", e);
        }
    }

    @Override
    public void reschedule(Long scheduledEmailId, LocalDateTime newScheduledAt) {
        validateLeadTime(newScheduledAt);
        try {
            ScheduledEmail scheduledEmail = scheduledEmailDao.findById(scheduledEmailId).orElse(null);
            if (scheduledEmail == null) {
                throw new IllegalArgumentException("Scheduled email does not exist.");
            }
            validateCanModify(scheduledEmail);

            LocalDateTime previousScheduledAt = scheduledEmail.scheduledAt;
            scheduledEmailDao.updateScheduledAt(scheduledEmailId, newScheduledAt);
            ScheduledFuture<?> future = activeTasks.remove(scheduledEmailId);
            if (future != null) {
                future.cancel(false);
            }

            scheduledEmail.scheduledAt = newScheduledAt;
            scheduledEmail.status = EmailStatus.SCHEDULED;
            scheduledEmail.retryCount = 0;
            scheduledEmail.errorMessage = null;
            try {
                register(scheduledEmail, Duration.between(LocalDateTime.now(), newScheduledAt));
            } catch (RuntimeException e) {
                scheduledEmailDao.updateScheduledAt(scheduledEmailId, previousScheduledAt);
                scheduledEmail.scheduledAt = previousScheduledAt;
                register(scheduledEmail, Duration.between(LocalDateTime.now(), previousScheduledAt));
                throw e;
            }
            LOGGER.info("Scheduled email rescheduled. taskId={}, newScheduledAt={}.", scheduledEmailId, newScheduledAt);
        } catch (IOException e) {
            throw new RuntimeException("Cannot reschedule scheduled email.", e);
        }
    }

    @Override
    public void updateQueuedEmail(Long scheduledEmailId, Email email, LocalDateTime newScheduledAt) {
        validateLeadTime(newScheduledAt);
        try {
            ScheduledEmail previous = scheduledEmailDao.findById(scheduledEmailId).orElse(null);
            if (previous == null) {
                throw new IllegalArgumentException("Scheduled email does not exist.");
            }
            validateCanModify(previous);

            Email updatedEmail = new Email(
                    previous.email.from,
                    email.toLst,
                    email.cc,
                    email.bcc,
                    email.subject,
                    email.content,
                    email.attachments,
                    null
            );
            scheduledEmailDao.updateQueuedEmail(scheduledEmailId, updatedEmail, newScheduledAt);

            ScheduledFuture<?> future = activeTasks.remove(scheduledEmailId);
            if (future != null) {
                future.cancel(false);
            }

            ScheduledEmail updated = new ScheduledEmail(previous.account, updatedEmail, newScheduledAt);
            updated.id = scheduledEmailId;
            updated.status = EmailStatus.SCHEDULED;

            try {
                register(updated, Duration.between(LocalDateTime.now(), newScheduledAt));
            } catch (RuntimeException e) {
                scheduledEmailDao.updateQueuedEmail(scheduledEmailId, previous.email, previous.scheduledAt);
                register(previous, Duration.between(LocalDateTime.now(), previous.scheduledAt));
                throw e;
            }

            LOGGER.info(
                    "Scheduled email updated by user. taskId={}, previousTarget={}, newTarget={}, status=SUCCESS.",
                    scheduledEmailId,
                    previous.scheduledAt,
                    newScheduledAt
            );
        } catch (IOException e) {
            throw new RuntimeException("Cannot update scheduled email.", e);
        }
    }

    @Override
    public void bootstrapPendingSchedules() {
        try {
            List<ScheduledEmail> pendingEmails = scheduledEmailDao.findByStatuses(
                    List.of(EmailStatus.SCHEDULED, EmailStatus.RETRY_PENDING)
            );
            LocalDateTime now = LocalDateTime.now();
            for (ScheduledEmail scheduledEmail : pendingEmails) {
                if (scheduledEmail.scheduledAt.isBefore(now)) {
                    scheduledEmailDao.updateStatus(
                            scheduledEmail.id,
                            EmailStatus.MISSED,
                            "Application was not running at the scheduled send time."
                    );
                    LOGGER.warn(
                            "Scheduled email marked missed during bootstrap. taskId={}, scheduledAt={}.",
                            scheduledEmail.id,
                            scheduledEmail.scheduledAt
                    );
                    continue;
                }
                register(scheduledEmail, Duration.between(now, scheduledEmail.scheduledAt));
            }
            LOGGER.info("Scheduler bootstrap completed. pendingRecordCount={}.", pendingEmails.size());
        } catch (IOException e) {
            LOGGER.error("Scheduler bootstrap failed.", e);
            throw new RuntimeException("Cannot bootstrap scheduled emails.", e);
        }
    }

    private void register(ScheduledEmail scheduledEmail, Duration delay) {
        if (scheduledEmail.id == null) {
            throw new IllegalArgumentException("Scheduled email must be persisted before registration.");
        }

        long delayMillis = Math.max(0, delay.toMillis());
        ScheduledFuture<?> previous = activeTasks.remove(scheduledEmail.id);
        if (previous != null) {
            previous.cancel(false);
        }

        ScheduledFuture<?> future = AppExecutors.scheduler().schedule(
                () -> AppExecutors.io().execute(() -> executeScheduledSend(scheduledEmail.id)),
                delayMillis,
                TimeUnit.MILLISECONDS
        );
        activeTasks.put(scheduledEmail.id, future);
    }

    private void executeScheduledSend(Long scheduledEmailId) {
        activeTasks.remove(scheduledEmailId);

        try {
            ScheduledEmail scheduledEmail = scheduledEmailDao.findById(scheduledEmailId).orElse(null);
            if (scheduledEmail == null || scheduledEmail.status == EmailStatus.CANCELLED) {
                return;
            }
            if (scheduledEmail.status != EmailStatus.SCHEDULED && scheduledEmail.status != EmailStatus.RETRY_PENDING) {
                LOGGER.info("Scheduled email ignored because status is no longer runnable. taskId={}, status={}.",
                        scheduledEmailId, scheduledEmail.status);
                return;
            }

            Account account = accountService.findByEmailAddress(scheduledEmail.email.from);
            if (account == null) {
                scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.FAILED, "Configured sender account was not found.");
                LOGGER.warn("Scheduled email failed because sender account is missing. taskId={}.", scheduledEmailId);
                return;
            }

            LOGGER.info(
                    "Scheduled email dispatch started. taskId={}, sender={}, scheduledAt={}.",
                    scheduledEmailId,
                    LogHelper.maskEmail(account.getUsername()),
                    scheduledEmail.scheduledAt
            );
            scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.SENDING, null);
            scheduledEmail.email.status = EmailStatus.SENDING;
            EmailUtil.send(account, scheduledEmail.email);
            scheduledEmail.email.status = EmailStatus.SENT;
            scheduledEmail.email.sentAt = LocalDateTime.now();
            emailDao.save(scheduledEmail.email);
            scheduledEmailDao.updateActualSentAt(scheduledEmailId);
            scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.SENT, null);
            LOGGER.info("Scheduled email sent successfully. taskId={}.", scheduledEmailId);
        } catch (MessagingException | IOException | RuntimeException e) {
            handleSendFailure(scheduledEmailId, e);
        }
    }

    private void handleSendFailure(Long scheduledEmailId, Exception error) {
        try {
            ScheduledEmail scheduledEmail = scheduledEmailDao.findById(scheduledEmailId).orElse(null);
            if (scheduledEmail == null) {
                return;
            }

            if (isNetworkFailure(error) && scheduledEmail.retryCount < MAX_NETWORK_RETRIES) {
                int nextRetryCount = scheduledEmail.retryCount + 1;
                LocalDateTime retryAt = LocalDateTime.now().plusSeconds(RETRY_DELAY_SECONDS);
                scheduledEmailDao.updateRetryState(
                        scheduledEmailId,
                        error.getMessage(),
                        nextRetryCount,
                        retryAt
                );
                scheduledEmail.retryCount = nextRetryCount;
                scheduledEmail.status = EmailStatus.RETRY_PENDING;
                scheduledEmail.scheduledAt = retryAt;
                register(scheduledEmail, Duration.between(LocalDateTime.now(), retryAt));
                LOGGER.warn(
                        "Scheduled email network failure; retry registered. taskId={}, retryCount={}/{}.",
                        scheduledEmailId,
                        nextRetryCount,
                        MAX_NETWORK_RETRIES,
                        error
                );
                return;
            }

            scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.FAILED, userFacingFailure(error));
            LOGGER.error("Scheduled email send failed permanently. taskId={}.", scheduledEmailId, error);
        } catch (IOException saveException) {
            LOGGER.error("Failed to persist scheduled email failure. taskId={}.", scheduledEmailId, saveException);
        }
    }

    private void validateLeadTime(LocalDateTime scheduledAt) {
        if (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now().plusSeconds(MINIMUM_LEAD_TIME_SECONDS))) {
            throw new IllegalArgumentException("Scheduled time must be at least 60 seconds in the future.");
        }
    }

    private void validateCanModify(ScheduledEmail scheduledEmail) {
        if (scheduledEmail.scheduledAt.isBefore(LocalDateTime.now().plusSeconds(MINIMUM_LEAD_TIME_SECONDS))) {
            throw new IllegalArgumentException("Scheduled email can only be changed or cancelled at least 60 seconds before send time.");
        }
    }

    private String userFacingFailure(Exception error) {
        if (isNetworkFailure(error)) {
            return "Network connection error after retry policy was exhausted: " + error.getMessage();
        }
        return error.getMessage();
    }

    private boolean isNetworkFailure(Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            String name = cause.getClass().getName();
            if (name.equals("java.net.UnknownHostException")
                    || name.equals("java.net.ConnectException")
                    || name.equals("java.net.SocketTimeoutException")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
