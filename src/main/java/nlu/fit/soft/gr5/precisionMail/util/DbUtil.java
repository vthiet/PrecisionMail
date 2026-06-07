package nlu.fit.soft.gr5.precisionMail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DbUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtil.class);

    private static final String DEFAULT_URL = "jdbc:sqlite:precisionmail.db";
    private static final String URL_PROPERTY = "precisionmail.db.url";

    public static Connection getConnect() throws SQLException {
        String url = System.getProperty(URL_PROPERTY, DEFAULT_URL);
        try {
            Connection connection = DriverManager.getConnection(url);
            try (Statement statement = connection.createStatement()) {
                statement.execute("pragma journal_mode=WAL");
                statement.execute("pragma foreign_keys=ON");
            }
            LOGGER.debug("Opened SQLite connection to {}.", url);
            return connection;
        } catch (SQLException e) {
            LOGGER.error("Failed to open SQLite connection to {}.", url, e);
            throw e;
        }
    }

    static void main() throws SQLException {
        Connection conn = getConnect();
        if (conn != null) {
            LOGGER.info("Database connection test completed successfully.");
        }
    }
}
