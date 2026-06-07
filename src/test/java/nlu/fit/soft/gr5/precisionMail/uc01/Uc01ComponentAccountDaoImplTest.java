package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.dao.impl.AccountDaoImpl;
import nlu.fit.soft.gr5.precisionMail.infrastructure.db.DatabaseInitializer;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uc01ComponentAccountDaoImplTest {
    private static final String DB_URL_PROPERTY = "precisionmail.db.url";

    @TempDir
    Path tempDir;

    private AccountDaoImpl accountDao;

    @BeforeEach
    void setUp() throws SQLException {
        System.setProperty(DB_URL_PROPERTY, "jdbc:sqlite:" + tempDir.resolve("component-uc01.db"));
        try (Connection connection = DbUtil.getConnect()) {
            DatabaseInitializer.initialize(connection);
        }
        accountDao = new AccountDaoImpl();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(DB_URL_PROPERTY);
    }

    @Test
    void saveAndFindAllMapAccountConfigurationThroughDaoInterface() {
        Account saved = account(
                "sender@gmail.com",
                "encrypted-password",
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

        accountDao.save(saved);
        List<Account> accounts = accountDao.findAll();
        Account loaded = accounts.getFirst();

        assertAll(
                () -> assertEquals(1, accounts.size()),
                () -> assertNotNull(saved.getId()),
                () -> assertEquals("sender@gmail.com", loaded.getUsername()),
                () -> assertEquals("Sender", loaded.getDisplayName()),
                () -> assertEquals("encrypted-password", loaded.getPassword()),
                () -> assertTrue(loaded.isPrimary()),
                () -> assertEquals("smtp.gmail.com", loaded.getMailServerConfig().getSmtpHost()),
                () -> assertEquals(587, loaded.getMailServerConfig().getSmtpPort()),
                () -> assertEquals("imap.gmail.com", loaded.getMailServerConfig().getImapHost()),
                () -> assertEquals(993, loaded.getMailServerConfig().getImapPort()),
                () -> assertEquals(SecurityMode.TLS, loaded.getMailServerConfig().getSmtpSecurityMode()),
                () -> assertEquals(SecurityMode.SSL, loaded.getMailServerConfig().getImapSecurityMode())
        );
    }

    @Test
    void saveUsesEmailUpsertInsteadOfCreatingDuplicateAccounts() {
        accountDao.save(account(
                "sender@gmail.com",
                "encrypted-v1",
                "Sender v1",
                true,
                new MailServerConfig()
        ));

        accountDao.save(account(
                "sender@gmail.com",
                "encrypted-v2",
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

        List<Account> accounts = accountDao.findAll();
        Account loaded = accounts.getFirst();

        assertAll(
                () -> assertEquals(1, accounts.size()),
                () -> assertEquals("encrypted-v2", loaded.getPassword()),
                () -> assertEquals("Sender v2", loaded.getDisplayName()),
                () -> assertEquals("smtp.office365.com", loaded.getMailServerConfig().getSmtpHost()),
                () -> assertEquals("outlook.office365.com", loaded.getMailServerConfig().getImapHost())
        );
    }

    @Test
    void primaryAccountInvariantIsMaintainedAcrossSaveAndDelete() {
        accountDao.save(account("first@gmail.com", "encrypted-1", "First", true, new MailServerConfig()));
        accountDao.save(account("second@gmail.com", "encrypted-2", "Second", true, new MailServerConfig()));

        List<Account> afterSecondPrimary = accountDao.findAll();

        assertAll(
                () -> assertEquals("second@gmail.com", afterSecondPrimary.getFirst().getUsername()),
                () -> assertTrue(afterSecondPrimary.getFirst().isPrimary()),
                () -> assertFalse(afterSecondPrimary.get(1).isPrimary())
        );

        accountDao.deleteByEmail("second@gmail.com");
        List<Account> afterDelete = accountDao.findAll();

        assertAll(
                () -> assertEquals(1, afterDelete.size()),
                () -> assertEquals("first@gmail.com", afterDelete.getFirst().getUsername()),
                () -> assertTrue(afterDelete.getFirst().isPrimary())
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
}
