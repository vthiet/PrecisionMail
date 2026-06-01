package nlu.fit.soft.gr5.precisionMail.service;

import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface QueueService {
    List<ScheduledEmail> findScheduled() throws IOException;

    void markCancelled(Long scheduledEmailId) throws IOException;

    void reschedule(Long scheduledEmailId, LocalDateTime scheduledAt) throws IOException;

    void updateQueuedEmail(Long scheduledEmailId, Email email, LocalDateTime scheduledAt) throws IOException;

    void markRetryPending(Long scheduledEmailId, String reason) throws IOException;

    List<ScheduledEmail> search(QueueSearchCriteria criteria)
            throws IOException;
}
