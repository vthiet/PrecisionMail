package nlu.fit.soft.gr5.precisionMail.model;

import java.time.LocalDateTime;

public class ScheduledEmail {
    public Account account;
    public Email email;
    public LocalDateTime scheduledAt;

    public ScheduledEmail() { }

    public ScheduledEmail(Account account, Email email, LocalDateTime scheduledAt) {
        this.account = account;
        this.email = email;
        this.scheduledAt = scheduledAt;
    }
}
