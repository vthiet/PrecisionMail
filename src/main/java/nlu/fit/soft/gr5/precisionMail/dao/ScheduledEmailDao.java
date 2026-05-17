package nlu.fit.soft.gr5.precisionMail.dao;

import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;

import java.io.IOException;
import java.util.List;

public interface ScheduledEmailDao {
    ScheduledEmail save(ScheduledEmail scheduledEmail) throws IOException;

    List<ScheduledEmail> findByStatus(EmailStatus status) throws IOException;

    void updateStatus(Long id, EmailStatus status, String errorMessage) throws IOException;
}
