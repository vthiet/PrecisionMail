package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.dao.ScheduledEmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.ScheduledEmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.QueueService;

import java.io.IOException;
import java.util.List;

public class QueueServiceImpl implements QueueService {
    private final ScheduledEmailDao scheduledEmailDao = new ScheduledEmailDaoImpl();

    @Override
    public List<ScheduledEmail> findScheduled() throws IOException {
        return scheduledEmailDao.findByStatus(EmailStatus.SCHEDULED);
    }

    @Override
    public void markCancelled(Long scheduledEmailId) throws IOException {
        scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.CANCELLED, null);
    }

    @Override
    public void markRetryPending(Long scheduledEmailId, String reason) throws IOException {
        scheduledEmailDao.updateStatus(scheduledEmailId, EmailStatus.RETRY_PENDING, reason);
    }
}
