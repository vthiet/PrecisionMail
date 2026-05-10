package nlu.fit.soft.gr5.precisionMail.dao.impl;

import nlu.fit.soft.gr5.precisionMail.dao.AccountDao;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountDaoImpl implements AccountDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDaoImpl.class);

    @Override
    public Account save(Account account) {

        String sql = """
                INSERT INTO accounts(
                    email,
                    encrypt_app_password,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?)
                ON CONFLICT(email) DO UPDATE SET
                    encrypt_app_password = excluded.encrypt_app_password,
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
            preparedStatement.setString(3, now);
            preparedStatement.setString(4, now);

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
        String sql = "select id, email, encrypt_app_password, created_at from accounts order by created_at asc";

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
                result.add(account);
            }

            LOGGER.info("Loaded {} account(s) from database.", result.size());
            return result;
        } catch (SQLException e) {
            LOGGER.error("Failed to load accounts from database.", e);
            throw new RuntimeException(e);
        }
    }
}
