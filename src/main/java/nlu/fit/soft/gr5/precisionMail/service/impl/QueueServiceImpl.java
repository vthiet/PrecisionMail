package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.dao.ScheduledEmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.ScheduledEmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.QueueSearchCriteria;
import nlu.fit.soft.gr5.precisionMail.service.QueueService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class QueueServiceImpl implements QueueService {
    private final ScheduledEmailDao scheduledEmailDao = new ScheduledEmailDaoImpl();
    private final ScheduledEmailServiceImpl scheduledEmailService = ScheduledEmailServiceImpl.getInstance();

    @Override
    public List<ScheduledEmail> findScheduled() throws IOException {
        return scheduledEmailDao.findByStatus(EmailStatus.SCHEDULED);
    }

    @Override
    public void markCancelled(Long scheduledEmailId) throws IOException {
        try {
            scheduledEmailService.cancel(scheduledEmailId);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to cancel scheduled email.", ex);
        }
    }

    @Override
    public void reschedule(Long scheduledEmailId, LocalDateTime scheduledAt) throws IOException {
        try {
            scheduledEmailService.reschedule(scheduledEmailId, scheduledAt);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to reschedule scheduled email.", ex);
        }
    }

    @Override
    public void updateQueuedEmail(Long scheduledEmailId, Email email, LocalDateTime scheduledAt) throws IOException {
        try {
            scheduledEmailService.updateQueuedEmail(scheduledEmailId, email, scheduledAt);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to update scheduled email.", ex);
        }
    }

    @Override
    public void markRetryPending(Long scheduledEmailId, String reason) throws IOException {
        scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.RETRY_PENDING, reason);
    }
    @Override
    public List<ScheduledEmail> search(QueueSearchCriteria criteria) throws IOException {

        List<ScheduledEmail> emails =
                scheduledEmailDao.findScheduled();

        return emails.stream()

                .filter(email -> {

                    if (criteria.getKeyword() == null
                            || criteria.getKeyword().isBlank()) {
                        return true;
                    }

                    String keyword =
                            criteria.getKeyword().toLowerCase();

                    return (email.email.subject != null
                            && email.email.subject.toLowerCase().contains(keyword))

                            || (email.email.from != null
                            && email.email.from.toLowerCase().contains(keyword))

                            || email.email.toLst.stream()
                            .anyMatch(to ->
                                    to.toLowerCase().contains(keyword));
                })

                .filter(email -> {

                    if (criteria.getStatus() == null) {
                        return true;
                    }

                    return email.status == criteria.getStatus();
                })

                .toList();
    }
}
