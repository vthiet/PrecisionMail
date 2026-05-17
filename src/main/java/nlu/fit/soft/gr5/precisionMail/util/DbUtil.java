package nlu.fit.soft.gr5.precisionMail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;

public class DbUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtil.class);

    private final static String URL = "jdbc:sqlite:precisionmail.db";

    public static Connection getConnect() throws SQLException {
        try {
            String folderPath = System.getProperty("user.home") + "\\PrecisionMail";
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();

            String dbPath = folderPath + "\\precisionmail.db";
            String url = "jdbc:sqlite:" + dbPath;

            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection(url);

            // 🔥 AUTO CREATE TABLE (QUAN TRỌNG)
            initDatabase(conn);

            return conn;

        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    private static void initDatabase(Connection conn) throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS accounts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            encrypt_app_password TEXT NOT NULL,
            created_at TEXT NOT NULL,
            updated_at TEXT NOT NULL
        )
    """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    static void main() throws SQLException {
        Connection conn = getConnect();
        if (conn != null) {
            LOGGER.info("Database connection test completed successfully.");
        }
    }
}
