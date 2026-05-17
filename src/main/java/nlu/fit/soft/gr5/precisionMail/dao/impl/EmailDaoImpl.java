package nlu.fit.soft.gr5.precisionMail.dao.impl;

import nlu.fit.soft.gr5.precisionMail.dao.EmailDao;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.service.HistorySearchCriteria;
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

public class EmailDaoImpl implements EmailDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailDaoImpl.class);
    private static final String DELIMITER = "\n";

    @Override
    public Email save(Email email) throws IOException {
        String now = LocalDateTime.now().toString();
        String sql = """
                insert into sent_emails(
                    sender_email,
                    to_recipients,
                    cc_recipients,
                    bcc_recipients,
                    subject,
                    body,
                    attachment_paths,
                    status,
                    error_message,
                    sent_at,
                    created_at,
                    updated_at
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email.from);
            ps.setString(2, join(email.toLst));
            ps.setString(3, join(email.cc));
            ps.setString(4, join(email.bcc));
            ps.setString(5, email.subject);
            ps.setString(6, email.content);
            ps.setString(7, join(email.attachments));
            ps.setString(8, statusOf(email).name());
            ps.setString(9, email.errorMessage);
            ps.setString(10, email.sentAt != null ? email.sentAt.toString() : null);
            ps.setString(11, now);
            ps.setString(12, now);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    email.id = rs.getLong(1);
                }
            }

            LOGGER.info("Email history saved to SQLite. id={}, status={}.", email.id, statusOf(email));
            return email;
        } catch (SQLException e) {
            LOGGER.error("Failed to save email history to SQLite.", e);
            throw new IOException("Failed to save email history.", e);
        }
    }

    @Override
    public List<Email> findAll() throws IOException {
        String sql = """
                select id,
                       sender_email,
                       to_recipients,
                       cc_recipients,
                       bcc_recipients,
                       subject,
                       body,
                       attachment_paths,
                       status,
                       error_message,
                       sent_at
                from sent_emails
                order by coalesce(sent_at, created_at) desc
                limit 1000
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Email> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapEmail(rs));
            }

            LOGGER.info("Loaded {} email history record(s) from SQLite.", result.size());
            return result;
        } catch (SQLException e) {
            LOGGER.error("Failed to read email history from SQLite.", e);
            throw new IOException("Failed to read email history.", e);
        }
    }

    @Override
    public List<Email> findHistory(HistorySearchCriteria criteria, int pageIndex, int pageSize) throws IOException {
        QueryParts query = buildHistoryWhere(criteria);
        String sql = """
                select id,
                       sender_email,
                       to_recipients,
                       cc_recipients,
                       bcc_recipients,
                       subject,
                       body,
                       attachment_paths,
                       status,
                       error_message,
                       sent_at
                from sent_emails
                %s
                order by coalesce(sent_at, created_at) desc
                limit ? offset ?
                """.formatted(query.whereClause());

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, query.params());
            int next = query.params().size() + 1;
            ps.setInt(next, pageSize);
            ps.setInt(next + 1, Math.max(0, pageIndex) * pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                List<Email> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapEmail(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to search email history from SQLite.", e);
            throw new IOException("Failed to search email history.", e);
        }
    }

    @Override
    public int countHistory(HistorySearchCriteria criteria) throws IOException {
        QueryParts query = buildHistoryWhere(criteria);
        String sql = "select count(*) from sent_emails " + query.whereClause();

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, query.params());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to count email history from SQLite.", e);
            throw new IOException("Failed to count email history.", e);
        }
    }

    @Override
    public Optional<Email> findById(Long id) throws IOException {
        if (id == null) {
            return Optional.empty();
        }

        String sql = """
                select id,
                       sender_email,
                       to_recipients,
                       cc_recipients,
                       bcc_recipients,
                       subject,
                       body,
                       attachment_paths,
                       status,
                       error_message,
                       sent_at
                from sent_emails
                where id = ?
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapEmail(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to read email history detail from SQLite. id={}.", id, e);
            throw new IOException("Failed to read email history detail.", e);
        }
    }

    @Override
    public List<String> findAllEmailAddress() throws IOException {
        return findAll().stream().map(Email::getFrom).distinct().toList();
    }

    private Email mapEmail(ResultSet rs) throws SQLException {
        Email email = new Email(
                rs.getString("sender_email"),
                splitSet(rs.getString("to_recipients")),
                splitSet(rs.getString("cc_recipients")),
                splitSet(rs.getString("bcc_recipients")),
                rs.getString("subject"),
                rs.getString("body"),
                splitList(rs.getString("attachment_paths")),
                parseDate(rs.getString("sent_at"))
        );
        email.id = rs.getLong("id");
        email.status = EmailStatus.valueOf(rs.getString("status"));
        email.errorMessage = rs.getString("error_message");
        return email;
    }

    private EmailStatus statusOf(Email email) {
        return email.status != null ? email.status : EmailStatus.SENT;
    }

    private LocalDateTime parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
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

    private QueryParts buildHistoryWhere(HistorySearchCriteria criteria) {
        List<String> clauses = new ArrayList<>();
        List<String> params = new ArrayList<>();

        if (criteria != null && !criteria.normalizedKeyword().isBlank()) {
            clauses.add("(lower(to_recipients) like ? or lower(cc_recipients) like ? or lower(bcc_recipients) like ? or lower(subject) like ?)");
            String keyword = "%" + criteria.normalizedKeyword().toLowerCase() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (criteria != null && criteria.startDate() != null) {
            clauses.add("date(coalesce(sent_at, created_at)) >= date(?)");
            params.add(criteria.startDate().toString());
        }

        if (criteria != null && criteria.endDate() != null) {
            clauses.add("date(coalesce(sent_at, created_at)) <= date(?)");
            params.add(criteria.endDate().toString());
        }

        return new QueryParts(clauses.isEmpty() ? "" : "where " + String.join(" and ", clauses), params);
    }

    private void bindParams(PreparedStatement ps, List<String> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setString(i + 1, params.get(i));
        }
    }

    private record QueryParts(String whereClause, List<String> params) {
    }
}
