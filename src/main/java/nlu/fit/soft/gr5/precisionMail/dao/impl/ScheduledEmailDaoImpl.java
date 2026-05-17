package nlu.fit.soft.gr5.precisionMail.dao.impl;

import nlu.fit.soft.gr5.precisionMail.dao.ScheduledEmailDao;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduledEmailDaoImpl implements ScheduledEmailDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledEmailDaoImpl.class);
    private static final String DELIMITER = "\n";

    @Override
    public ScheduledEmail save(ScheduledEmail scheduledEmail) throws IOException {
        String now = LocalDateTime.now().toString();
        String sql = """
                insert into scheduled_emails(
                    account_id,
                    sender_email,
                    to_recipients,
                    cc_recipients,
                    bcc_recipients,
                    subject,
                    body,
                    attachment_paths,
                    scheduled_at,
                    status,
                    error_message,
                    retry_count,
                    actual_sent_at,
                    created_at,
                    updated_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Email email = scheduledEmail.email;
            if (scheduledEmail.account != null && scheduledEmail.account.getId() != null) {
                ps.setLong(1, scheduledEmail.account.getId());
            } else {
                ps.setObject(1, null);
            }
            ps.setString(2, email.from);
            ps.setString(3, join(email.toLst));
            ps.setString(4, join(email.cc));
            ps.setString(5, join(email.bcc));
            ps.setString(6, email.subject);
            ps.setString(7, email.content);
            ps.setString(8, join(email.attachments));
            ps.setString(9, scheduledEmail.scheduledAt.toString());
            ps.setString(10, statusOf(scheduledEmail).name());
            ps.setString(11, scheduledEmail.errorMessage);
            ps.setInt(12, scheduledEmail.retryCount);
            ps.setString(13, scheduledEmail.actualSentAt != null ? scheduledEmail.actualSentAt.toString() : null);
            ps.setString(14, now);
            ps.setString(15, now);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    scheduledEmail.id = rs.getLong(1);
                }
            }

            LOGGER.info("Scheduled email persisted. id={}, status={}.", scheduledEmail.id, statusOf(scheduledEmail));
            return scheduledEmail;
        } catch (SQLException e) {
            LOGGER.error("Failed to persist scheduled email.", e);
            throw new IOException("Failed to persist scheduled email.", e);
        }
    }

    @Override
    public List<ScheduledEmail> findByStatus(EmailStatus status) throws IOException {
        String sql = """
                select id,
                       account_id,
                       sender_email,
                       to_recipients,
                       cc_recipients,
                       bcc_recipients,
                       subject,
                       body,
                       attachment_paths,
                       scheduled_at,
                       status,
                       error_message,
                       retry_count,
                       actual_sent_at
                from scheduled_emails
                where status = ?
                order by scheduled_at asc
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<ScheduledEmail> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapScheduledEmail(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load scheduled emails by status={}.", status, e);
            throw new IOException("Failed to load scheduled emails.", e);
        }
    }

    @Override
    public List<ScheduledEmail> findByStatuses(List<EmailStatus> statuses) throws IOException {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        String placeholders = statuses.stream().map(status -> "?").collect(Collectors.joining(", "));
        String sql = """
                select id,
                       account_id,
                       sender_email,
                       to_recipients,
                       cc_recipients,
                       bcc_recipients,
                       subject,
                       body,
                       attachment_paths,
                       scheduled_at,
                       status,
                       error_message,
                       retry_count,
                       actual_sent_at
                from scheduled_emails
                where status in (%s)
                order by scheduled_at asc
                """.formatted(placeholders);

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < statuses.size(); i++) {
                ps.setString(i + 1, statuses.get(i).name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ScheduledEmail> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapScheduledEmail(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load scheduled emails by statuses={}.", statuses, e);
            throw new IOException("Failed to load scheduled emails.", e);
        }
    }

    @Override
    public Optional<ScheduledEmail> findById(Long id) throws IOException {
        if (id == null) {
            return Optional.empty();
        }

        String sql = """
                select id,
                       account_id,
                       sender_email,
                       to_recipients,
                       cc_recipients,
                       bcc_recipients,
                       subject,
                       body,
                       attachment_paths,
                       scheduled_at,
                       status,
                       error_message,
                       retry_count,
                       actual_sent_at
                from scheduled_emails
                where id = ?
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapScheduledEmail(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load scheduled email by id={}.", id, e);
            throw new IOException("Failed to load scheduled email.", e);
        }
    }

    @Override
    public void updateStatus(Long id, EmailStatus status, String errorMessage) throws IOException {
        if (id == null) {
            return;
        }

        String sql = """
                update scheduled_emails
                set status = ?, error_message = ?, updated_at = ?
                where id = ?
                """;
        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, errorMessage);
            ps.setString(3, LocalDateTime.now().toString());
            ps.setLong(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update scheduled email status. id={}, status={}.", id, status, e);
            throw new IOException("Failed to update scheduled email.", e);
        }
    }

    @Override
    public void updateStatusAndRetryCount(Long id, EmailStatus status, String errorMessage, int retryCount) throws IOException {
        if (id == null) {
            return;
        }

        String sql = """
                update scheduled_emails
                set status = ?, error_message = ?, retry_count = ?, updated_at = ?
                where id = ?
                """;
        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, errorMessage);
            ps.setInt(3, retryCount);
            ps.setString(4, LocalDateTime.now().toString());
            ps.setLong(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update scheduled email retry count. id={}, status={}.", id, status, e);
            throw new IOException("Failed to update scheduled email.", e);
        }
    }

    @Override
    public void updateRetryState(Long id, String errorMessage, int retryCount, LocalDateTime retryAt) throws IOException {
        if (id == null || retryAt == null) {
            return;
        }

        String sql = """
                update scheduled_emails
                set status = ?, error_message = ?, retry_count = ?, scheduled_at = ?, updated_at = ?
                where id = ?
                """;
        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, EmailStatus.RETRY_PENDING.name());
            ps.setString(2, errorMessage);
            ps.setInt(3, retryCount);
            ps.setString(4, retryAt.toString());
            ps.setString(5, LocalDateTime.now().toString());
            ps.setLong(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update scheduled email retry state. id={}, retryAt={}.", id, retryAt, e);
            throw new IOException("Failed to update scheduled email.", e);
        }
    }

    @Override
    public void updateActualSentAt(Long id) throws IOException {
        if (id == null) {
            return;
        }

        String now = LocalDateTime.now().toString();
        String sql = """
                update scheduled_emails
                set actual_sent_at = ?, updated_at = ?
                where id = ?
                """;
        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, now);
            ps.setString(2, now);
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update scheduled email sent time. id={}.", id, e);
            throw new IOException("Failed to update scheduled email.", e);
        }
    }

    @Override
    public void updateScheduledAt(Long id, LocalDateTime scheduledAt) throws IOException {
        if (id == null || scheduledAt == null) {
            return;
        }

        String sql = """
                update scheduled_emails
                set scheduled_at = ?, status = ?, error_message = null, retry_count = 0, updated_at = ?
                where id = ?
                """;
        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, scheduledAt.toString());
            ps.setString(2, EmailStatus.SCHEDULED.name());
            ps.setString(3, LocalDateTime.now().toString());
            ps.setLong(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update scheduled email time. id={}, scheduledAt={}.", id, scheduledAt, e);
            throw new IOException("Failed to update scheduled email.", e);
        }
    }

    @Override
    public void updateQueuedEmail(Long id, Email email, LocalDateTime scheduledAt) throws IOException {
        if (id == null || email == null || scheduledAt == null) {
            return;
        }

        String sql = """
                update scheduled_emails
                set to_recipients = ?,
                    cc_recipients = ?,
                    bcc_recipients = ?,
                    subject = ?,
                    body = ?,
                    attachment_paths = ?,
                    scheduled_at = ?,
                    status = ?,
                    error_message = null,
                    retry_count = 0,
                    updated_at = ?
                where id = ?
                """;
        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, join(email.toLst));
            ps.setString(2, join(email.cc));
            ps.setString(3, join(email.bcc));
            ps.setString(4, email.subject);
            ps.setString(5, email.content);
            ps.setString(6, join(email.attachments));
            ps.setString(7, scheduledAt.toString());
            ps.setString(8, EmailStatus.SCHEDULED.name());
            ps.setString(9, LocalDateTime.now().toString());
            ps.setLong(10, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update queued scheduled email. id={}, scheduledAt={}.", id, scheduledAt, e);
            throw new IOException("Failed to update scheduled email.", e);
        }
    }

    private ScheduledEmail mapScheduledEmail(ResultSet rs) throws SQLException {
        Account account = new Account(rs.getString("sender_email"), null, null);
        long accountId = rs.getLong("account_id");
        if (!rs.wasNull()) {
            account.setId(accountId);
        }

        Email email = new Email(
                rs.getString("sender_email"),
                splitSet(rs.getString("to_recipients")),
                splitSet(rs.getString("cc_recipients")),
                splitSet(rs.getString("bcc_recipients")),
                rs.getString("subject"),
                rs.getString("body"),
                splitList(rs.getString("attachment_paths")),
                null
        );
        email.id = rs.getLong("id");

        ScheduledEmail scheduledEmail = new ScheduledEmail(
                account,
                email,
                LocalDateTime.parse(rs.getString("scheduled_at"))
        );
        scheduledEmail.id = rs.getLong("id");
        scheduledEmail.status = EmailStatus.valueOf(rs.getString("status"));
        scheduledEmail.errorMessage = rs.getString("error_message");
        scheduledEmail.retryCount = rs.getInt("retry_count");
        String actualSentAt = rs.getString("actual_sent_at");
        scheduledEmail.actualSentAt = actualSentAt == null || actualSentAt.isBlank()
                ? null
                : LocalDateTime.parse(actualSentAt);
        return scheduledEmail;
    }

    private EmailStatus statusOf(ScheduledEmail scheduledEmail) {
        return scheduledEmail.status != null ? scheduledEmail.status : EmailStatus.SCHEDULED;
    }

    private String join(Set<String> values) {
        return values == null || values.isEmpty() ? "" : String.join(DELIMITER, values);
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? "" : String.join(DELIMITER, values);
    }

    private Set<String> splitSet(String value) {
        return new LinkedHashSet<>(splitList(value));
    }

    private List<String> splitList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(DELIMITER)));
    }
}
