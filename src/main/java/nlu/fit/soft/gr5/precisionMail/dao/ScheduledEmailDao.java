package nlu.fit.soft.gr5.precisionMail.dao;

import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.QueueSearchCriteria;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ScheduledEmailDao {
    ScheduledEmail save(ScheduledEmail scheduledEmail) throws IOException;

    List<ScheduledEmail> findByStatus(EmailStatus status) throws IOException;

    List<ScheduledEmail> findByStatuses(List<EmailStatus> statuses) throws IOException;

    Optional<ScheduledEmail> findById(Long id) throws IOException;

    void updateStatus(Long id, EmailStatus status, String errorMessage) throws IOException;

    void updateStatusAndRetryCount(Long id, EmailStatus status, String errorMessage, int retryCount) throws IOException;

    void updateRetryState(Long id, String errorMessage, int retryCount, java.time.LocalDateTime retryAt) throws IOException;

    void updateActualSentAt(Long id) throws IOException;

    void updateScheduledAt(Long id, java.time.LocalDateTime scheduledAt) throws IOException;

    void updateQueuedEmail(Long id, nlu.fit.soft.gr5.precisionMail.model.Email email, java.time.LocalDateTime scheduledAt) throws IOException;

    List<ScheduledEmail> search(QueueSearchCriteria criteria) throws IOException;

    List<ScheduledEmail> findScheduled() throws IOException;
    void delete(Long id) throws IOException;
}
