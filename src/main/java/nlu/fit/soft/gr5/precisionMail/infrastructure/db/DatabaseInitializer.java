package nlu.fit.soft.gr5.precisionMail.infrastructure.db;

import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public final class DatabaseInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DbUtil.getConnect();
             Statement st = connection.createStatement()) {
            createAccountsTable(st);
            migrateAccountsTableIfNeeded(connection);
            createSentEmailsTable(st);
            createScheduledEmailsTable(st);
            migrateScheduledEmailsTableIfNeeded(connection);
            createIndexes(st);
            LOGGER.info("Database initialization completed successfully.");
        } catch (SQLException e) {
            LOGGER.error("Database initialization failed.", e);
            throw new RuntimeException(e);
        }
    }

    private static void createAccountsTable(Statement st) throws SQLException {
        st.execute("""
                create table if not exists accounts
                (
                    id integer primary key autoincrement,
                    email text not null unique,
                    encrypt_app_password text not null,
                    smtp_host text not null default 'smtp.gmail.com',
                    smtp_port integer not null default 587,
                    imap_host text not null default 'imap.gmail.com',
                    imap_port integer not null default 993,
                    security_mode text not null default 'TLS',
                    smtp_security_mode text not null default 'TLS',
                    imap_security_mode text not null default 'SSL',
                    created_at text not null,
                    updated_at text not null
                );
                """);
    }

    private static void createSentEmailsTable(Statement st) throws SQLException {
        st.execute("""
                create table if not exists sent_emails
                (
                    id integer primary key autoincrement,
                    sender_email text not null,
                    to_recipients text not null default '',
                    cc_recipients text not null default '',
                    bcc_recipients text not null default '',
                    subject text,
                    body text,
                    attachment_paths text,
                    status text not null,
                    error_message text,
                    sent_at text,
                    created_at text not null,
                    updated_at text not null
                );
                """);
    }

    private static void createScheduledEmailsTable(Statement st) throws SQLException {
        st.execute("""
                create table if not exists scheduled_emails
                (
                    id integer primary key autoincrement,
                    account_id integer,
                    sender_email text not null,
                    to_recipients text not null default '',
                    cc_recipients text not null default '',
                    bcc_recipients text not null default '',
                    subject text,
                    body text,
                    attachment_paths text,
                    scheduled_at text not null,
                    status text not null,
                    error_message text,
                    retry_count integer not null default 0,
                    actual_sent_at text,
                    created_at text not null,
                    updated_at text not null,
                    foreign key(account_id) references accounts(id)
                );
                """);
    }

    private static void createIndexes(Statement st) throws SQLException {
        st.execute("create index if not exists idx_sent_emails_sent_at on sent_emails(sent_at)");
        st.execute("create index if not exists idx_sent_emails_status on sent_emails(status)");
        st.execute("create index if not exists idx_sent_emails_sender on sent_emails(sender_email)");
        st.execute("create index if not exists idx_sent_emails_to_recipients on sent_emails(to_recipients)");
        st.execute("create index if not exists idx_scheduled_emails_status on scheduled_emails(status)");
        st.execute("create index if not exists idx_scheduled_emails_scheduled_at on scheduled_emails(scheduled_at)");
    }

    private static void migrateAccountsTableIfNeeded(Connection connection) throws SQLException {
        Set<String> columns = getTableColumns(connection, "accounts");
        if (columns.containsAll(Set.of(
                "email",
                "encrypt_app_password",
                "smtp_host",
                "smtp_port",
                "imap_host",
                "imap_port",
                "security_mode",
                "smtp_security_mode",
                "imap_security_mode",
                "created_at",
                "updated_at"
        ))) {
            return;
        }

        if (!columns.contains("email") && !columns.contains("username")) {
            throw new SQLException("Cannot migrate accounts table: missing email/username column.");
        }

        if (!columns.contains("encrypt_app_password") && !columns.contains("password")) {
            throw new SQLException("Cannot migrate accounts table: missing password column.");
        }

        String emailColumn = columns.contains("email") ? "email" : "username";
        String passwordColumn = columns.contains("encrypt_app_password") ? "encrypt_app_password" : "password";
        String createdAtExpression = columns.contains("created_at") ? "created_at" : "datetime('now')";
        String updatedAtExpression = columns.contains("updated_at") ? "updated_at" : createdAtExpression;
        String smtpHostExpression = columns.contains("smtp_host") ? "smtp_host" : "'smtp.gmail.com'";
        String smtpPortExpression = columns.contains("smtp_port") ? "smtp_port" : "587";
        String imapHostExpression = columns.contains("imap_host") ? "imap_host" : "'imap.gmail.com'";
        String imapPortExpression = columns.contains("imap_port") ? "imap_port" : "993";
        String securityModeExpression = columns.contains("security_mode") ? "security_mode" : "'TLS'";
        String smtpSecurityModeExpression = columns.contains("smtp_security_mode") ? "smtp_security_mode" : securityModeExpression;
        String imapSecurityModeExpression = columns.contains("imap_security_mode")
                ? "imap_security_mode"
                : "case when " + imapPortExpression + " = 993 then 'SSL' else " + securityModeExpression + " end";

        try (Statement st = connection.createStatement()) {
            st.execute("alter table accounts rename to accounts_legacy");
            createAccountsTable(st);
            st.execute("""
                    insert or ignore into accounts(
                        id,
                        email,
                        encrypt_app_password,
                        smtp_host,
                        smtp_port,
                        imap_host,
                        imap_port,
                        security_mode,
                        smtp_security_mode,
                        imap_security_mode,
                        created_at,
                        updated_at
                    )
                    select id, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
                    from accounts_legacy
                    where %s is not null and trim(%s) <> ''
                    """.formatted(
                    emailColumn,
                    passwordColumn,
                    smtpHostExpression,
                    smtpPortExpression,
                    imapHostExpression,
                    imapPortExpression,
                    securityModeExpression,
                    smtpSecurityModeExpression,
                    imapSecurityModeExpression,
                    createdAtExpression,
                    updatedAtExpression,
                    emailColumn,
                    emailColumn
            ));
            st.execute("drop table accounts_legacy");
        }

        LOGGER.info("Migrated accounts table to current schema.");
    }

    private static Set<String> getTableColumns(Connection connection, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("pragma table_info(" + tableName + ")")) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        return columns;
    }

    private static void migrateScheduledEmailsTableIfNeeded(Connection connection) throws SQLException {
        Set<String> columns = getTableColumns(connection, "scheduled_emails");
        try (Statement st = connection.createStatement()) {
            if (!columns.contains("retry_count")) {
                st.execute("alter table scheduled_emails add column retry_count integer not null default 0");
            }
            if (!columns.contains("actual_sent_at")) {
                st.execute("alter table scheduled_emails add column actual_sent_at text");
            }
        }
    }
}
