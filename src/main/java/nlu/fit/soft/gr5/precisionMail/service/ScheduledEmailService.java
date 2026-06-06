package nlu.fit.soft.gr5.precisionMail.service;

import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;

public interface ScheduledEmailService {

    ScheduledEmail schedule(ScheduledEmail scheduledEmail);

    void cancel(Long scheduledEmailId);

    void reschedule(Long scheduledEmailId, java.time.LocalDateTime newScheduledAt);

    void updateQueuedEmail(Long scheduledEmailId, nlu.fit.soft.gr5.precisionMail.model.Email email, java.time.LocalDateTime newScheduledAt);

    void bootstrapPendingSchedules();

    void pauseQueue();

    void resumeQueue();

    boolean isQueuePaused();
}
