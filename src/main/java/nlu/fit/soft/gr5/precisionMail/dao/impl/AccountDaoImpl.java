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
                    username,
                    password,
                    created_at
                )
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = DbUtil.getConnect();

                PreparedStatement preparedStatement =
                        conn.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            preparedStatement.setString(1, account.getUsername());
            preparedStatement.setString(2, account.getPassword());
            preparedStatement.setString(3, LocalDateTime.now().toString());

            preparedStatement.executeUpdate();

            try (ResultSet rs =
                         preparedStatement.getGeneratedKeys()) {

                if (rs.next()) {
                    account.setId(rs.getLong(1));
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
        String sql = "select * from accounts";

        try (Connection connection = DbUtil.getConnect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery();) {
            List<Account> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Account(
                        rs.getString("username"),
                        rs.getString("password"),
                        LocalDateTime.parse(rs.getString("created_at"))
                ));
            }

            LOGGER.info("Loaded {} account(s) from database.", result.size());
            return result;
        } catch (SQLException e) {
            LOGGER.error("Failed to load accounts from database.", e);
            throw new RuntimeException(e);
        }
    }
}
