package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.infrastructure.db.DatabaseInitializer;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uc01SystemAccountConfigurationFlowTest {
    private static final String DB_URL_PROPERTY = "precisionmail.db.url";

    @TempDir
    Path tempDir;

    private String dbUrl;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() throws SQLException {
        dbUrl = "jdbc:sqlite:" + tempDir.resolve("system-uc01.db");
        System.setProperty(DB_URL_PROPERTY, dbUrl);
        try (Connection connection = DbUtil.getConnect()) {
            DatabaseInitializer.initialize(connection);
        }
        accountService = new AccountServiceImpl();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(DB_URL_PROPERTY);
    }

    @Test
    void saveThenLoadConfigurationEncryptsDatabasePasswordAndRestoresPlaintextForController() throws SQLException {
        Account account = account(
                "sender@gmail.com",
                "abcd efgh ijkl mnop",
                "Sender",
                true,
                new MailServerConfig(
                        "smtp.gmail.com",
                        587,
                        "imap.gmail.com",
                        993,
                        SecurityMode.TLS,
                        SecurityMode.SSL
                )
        );

        accountService.save(account);

        RawAccountRow raw = readOnlyAccountRow();
        Account loaded = accountService.findPrimaryConfiguration();

        assertAll(
                () -> assertEquals(1, raw.count()),
                () -> assertEquals("sender@gmail.com", raw.email()),
                () -> assertNotEquals("abcd efgh ijkl mnop", raw.encryptedPassword()),
                () -> assertTrue(raw.encryptedPassword().startsWith("v2:")),
                () -> assertEquals("abcd efgh ijkl mnop", loaded.getPassword()),
                () -> assertFalse(loaded.isPasswordDecryptionFailed()),
                () -> assertEquals("smtp.gmail.com", loaded.getMailServerConfig().getSmtpHost()),
                () -> assertEquals(SecurityMode.TLS, loaded.getMailServerConfig().getSmtpSecurityMode()),
                () -> assertEquals(SecurityMode.SSL, loaded.getMailServerConfig().getImapSecurityMode())
        );
    }

    @Test
    void savingSameEmailRunsCompleteUpsertFlowWithoutDuplicatingRows() throws SQLException {
        accountService.save(account(
                "sender@gmail.com",
                "first-password",
                "Sender v1",
                true,
                new MailServerConfig()
        ));
        accountService.save(account(
                "sender@gmail.com",
                "second-password",
                "Sender v2",
                true,
                new MailServerConfig(
                        "smtp.office365.com",
                        587,
                        "outlook.office365.com",
                        993,
                        SecurityMode.TLS,
                        SecurityMode.SSL
                )
        ));

        RawAccountRow raw = readOnlyAccountRow();
        Account loaded = accountService.findByEmailAddress("sender@gmail.com");

        assertAll(
                () -> assertEquals(1, raw.count()),
                () -> assertEquals("Sender v2", raw.displayName()),
                () -> assertEquals("second-password", loaded.getPassword()),
                () -> assertEquals("smtp.office365.com", loaded.getMailServerConfig().getSmtpHost()),
                () -> assertEquals("outlook.office365.com", loaded.getMailServerConfig().getImapHost())
        );
    }

    private Account account(
            String username,
            String password,
            String displayName,
            boolean primary,
            MailServerConfig config
    ) {
        Account account = new Account(username, password, LocalDateTime.of(2026, 6, 7, 10, 30));
        account.setDisplayName(displayName);
        account.setPrimary(primary);
        account.setMailServerConfig(config);
        return account;
    }

    private RawAccountRow readOnlyAccountRow() throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbUrl);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                     select count(*) as row_count,
                            email,
                            display_name,
                            encrypt_app_password
                     from accounts
                     """)) {
            assertTrue(rs.next());
            return new RawAccountRow(
                    rs.getInt("row_count"),
                    rs.getString("email"),
                    rs.getString("display_name"),
                    rs.getString("encrypt_app_password")
            );
        }
    }

    private record RawAccountRow(int count, String email, String displayName, String encryptedPassword) {
    }
}
