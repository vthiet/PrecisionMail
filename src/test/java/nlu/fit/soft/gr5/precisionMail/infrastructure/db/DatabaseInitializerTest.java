package nlu.fit.soft.gr5.precisionMail.infrastructure.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInitializerTest {

    @Test
    void migratesLegacyAccountsSecurityModeIntoSeparateSmtpAndImapModes() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             Statement statement = connection.createStatement()) {
            createLegacyAccountsTable(statement);

            DatabaseInitializer.initialize(connection);

            Set<String> columns = accountColumns(statement);
            assertAll(
                    () -> assertTrue(columns.contains("security_mode")),
                    () -> assertTrue(columns.contains("smtp_security_mode")),
                    () -> assertTrue(columns.contains("imap_security_mode"))
            );

            try (ResultSet rs = statement.executeQuery("""
                    select email,
                           smtp_security_mode,
                           imap_security_mode
                    from accounts
                    order by email
                    """)) {
                assertTrue(rs.next());
                assertAll(
                        () -> assertEquals("imap143@example.com", rs.getString("email")),
                        () -> assertEquals("TLS", rs.getString("smtp_security_mode")),
                        () -> assertEquals("TLS", rs.getString("imap_security_mode"))
                );

                assertTrue(rs.next());
                assertAll(
                        () -> assertEquals("imap993@example.com", rs.getString("email")),
                        () -> assertEquals("TLS", rs.getString("smtp_security_mode")),
                        () -> assertEquals("SSL", rs.getString("imap_security_mode"))
                );
            }
        }
    }

    private void createLegacyAccountsTable(Statement statement) throws SQLException {
        statement.execute("""
                create table accounts
                (
                    id integer primary key autoincrement,
                    email text not null unique,
                    encrypt_app_password text not null,
                    smtp_host text not null,
                    smtp_port integer not null,
                    imap_host text not null,
                    imap_port integer not null,
                    security_mode text not null,
                    created_at text not null,
                    updated_at text not null
                )
                """);
        statement.execute("""
                insert into accounts(
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
                values
                    ('imap143@example.com', 'encrypted-1', 'smtp.example.com', 587, 'imap.example.com', 143, 'TLS', '2026-06-01T10:00:00', '2026-06-01T10:00:00'),
                    ('imap993@example.com', 'encrypted-2', 'smtp.example.com', 587, 'imap.example.com', 993, 'TLS', '2026-06-01T10:00:00', '2026-06-01T10:00:00')
                """);
    }

    private Set<String> accountColumns(Statement statement) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = statement.executeQuery("pragma table_info(accounts)")) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        return columns;
    }
}
