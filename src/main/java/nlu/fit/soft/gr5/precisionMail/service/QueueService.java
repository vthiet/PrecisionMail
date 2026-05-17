package nlu.fit.soft.gr5.precisionMail.service;

import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;

import java.io.IOException;
import java.util.List;

public interface QueueService {
    List<ScheduledEmail> findScheduled() throws IOException;

    void markCancelled(Long scheduledEmailId) throws IOException;

    void markRetryPending(Long scheduledEmailId, String reason) throws IOException;
}
