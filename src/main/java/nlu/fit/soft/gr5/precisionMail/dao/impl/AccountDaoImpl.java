package nlu.fit.soft.gr5.precisionMail.dao.impl;

import nlu.fit.soft.gr5.precisionMail.dao.AccountDao;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDaoImpl implements AccountDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDaoImpl.class);

    @Override
    public Account save(Account account) {

        String sql = """
                INSERT INTO accounts(
                    email,
                    encrypt_app_password,
                    smtp_host,
                    smtp_port,
                    imap_host,
                    imap_port,
                    security_mode,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(email) DO UPDATE SET
                    encrypt_app_password = excluded.encrypt_app_password,
                    smtp_host = excluded.smtp_host,
                    smtp_port = excluded.smtp_port,
                    imap_host = excluded.imap_host,
                    imap_port = excluded.imap_port,
                    security_mode = excluded.security_mode,
                    updated_at = excluded.updated_at
                """;

        try (
                Connection conn = DbUtil.getConnect();

                PreparedStatement preparedStatement =
                        conn.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            String now = LocalDateTime.now().toString();

            preparedStatement.setString(1, account.getUsername());
            preparedStatement.setString(2, account.getPassword());
            MailServerConfig config = account.getMailServerConfig();
            preparedStatement.setString(3, config.getSmtpHost());
            preparedStatement.setInt(4, config.getSmtpPort());
            preparedStatement.setString(5, config.getImapHost());
            preparedStatement.setInt(6, config.getImapPort());
            preparedStatement.setString(7, config.getSecurityMode().name());
            preparedStatement.setString(8, now);
            preparedStatement.setString(9, now);

            preparedStatement.executeUpdate();

            try (ResultSet rs =
                         preparedStatement.getGeneratedKeys()) {

                if (rs.next()) {
                    account.setId(rs.getLong(1));
                }
            }

            if (account.getId() == null) {
                try (PreparedStatement selectStatement = conn.prepareStatement(
                        "select id from accounts where email = ?"
                )) {
                    selectStatement.setString(1, account.getUsername());
                    try (ResultSet rs = selectStatement.executeQuery()) {
                        if (rs.next()) {
                            account.setId(rs.getLong("id"));
                        }
                    }
                }
            }

            LOGGER.info(
                    "Account persisted successfully for username={}.",
                    LogHelper.maskEmail(account.getUsername())
            );

        } catch (SQLException e) {
            LOGGER.error(
                    "Failed to persist account for username={}.",
                    LogHelper.maskEmail(account.getUsername()),
                    e
            );
            throw new RuntimeException(e);
        }

        return account;
    }

    @Override
    public List<Account> findAll() {
        String sql = """
                select id,
                       email,
                       encrypt_app_password,
                       smtp_host,
                       smtp_port,
                       imap_host,
                       imap_port,
                       security_mode,
                       created_at
                from accounts
                order by created_at asc
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery();) {
            List<Account> result = new ArrayList<>();
            while (rs.next()) {
                Account account = new Account(
                        rs.getString("email"),
                        rs.getString("encrypt_app_password"),
                        LocalDateTime.parse(rs.getString("created_at"))
                );
                account.setId(rs.getLong("id"));
                account.setMailServerConfig(new MailServerConfig(
                        rs.getString("smtp_host"),
                        rs.getInt("smtp_port"),
                        rs.getString("imap_host"),
                        rs.getInt("imap_port"),
                        parseSecurityMode(rs.getString("security_mode"))
                ));
                result.add(account);
            }

            LOGGER.info("Loaded {} account(s) from database.", result.size());
            return result;
        } catch (SQLException e) {
            LOGGER.error("Failed to load accounts from database.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String sql = """
                select id,
                       email,
                       encrypt_app_password,
                       smtp_host,
                       smtp_port,
                       imap_host,
                       imap_port,
                       security_mode,
                       created_at
                from accounts
                where lower(email) = lower(?)
                limit 1
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapAccount(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load account by email={}.", LogHelper.maskEmail(email), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Account account) {
        if (account == null || account.getUsername() == null || account.getUsername().isBlank()) {
            return;
        }

        String sql = """
                update accounts
                set encrypt_app_password = ?,
                    smtp_host = ?,
                    smtp_port = ?,
                    imap_host = ?,
                    imap_port = ?,
                    security_mode = ?,
                    updated_at = ?
                where lower(email) = lower(?)
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            MailServerConfig config = account.getMailServerConfig();
            preparedStatement.setString(1, account.getPassword());
            preparedStatement.setString(2, config.getSmtpHost());
            preparedStatement.setInt(3, config.getSmtpPort());
            preparedStatement.setString(4, config.getImapHost());
            preparedStatement.setInt(5, config.getImapPort());
            preparedStatement.setString(6, config.getSecurityMode().name());
            preparedStatement.setString(7, LocalDateTime.now().toString());
            preparedStatement.setString(8, account.getUsername());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Account updated successfully for username={}.", LogHelper.maskEmail(account.getUsername()));
            } else {
                LOGGER.warn("No account found to update for username={}.", LogHelper.maskEmail(account.getUsername()));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to update account for username={}.", LogHelper.maskEmail(account.getUsername()), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByEmail(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        String sql = """
                delete from accounts
                where lower(email) = lower(?)
                """;

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Account deleted successfully for username={}.", LogHelper.maskEmail(email));
            } else {
                LOGGER.warn("No account found to delete for username={}.", LogHelper.maskEmail(email));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to delete account for username={}.", LogHelper.maskEmail(email), e);
            throw new RuntimeException(e);
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        Account account = new Account(
                rs.getString("email"),
                rs.getString("encrypt_app_password"),
                LocalDateTime.parse(rs.getString("created_at"))
        );
        account.setId(rs.getLong("id"));
        account.setMailServerConfig(new MailServerConfig(
                rs.getString("smtp_host"),
                rs.getInt("smtp_port"),
                rs.getString("imap_host"),
                rs.getInt("imap_port"),
                parseSecurityMode(rs.getString("security_mode"))
        ));
        return account;
    }

    private SecurityMode parseSecurityMode(String value) {
        if (value == null || value.isBlank()) {
            return SecurityMode.TLS;
        }
        try {
            return SecurityMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Unknown security mode in database: {}. Falling back to TLS.", value);
            return SecurityMode.TLS;
        }
    }
}
