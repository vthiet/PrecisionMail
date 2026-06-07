package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.dao.AccountDao;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Uc01AccountServiceImplTest {
    private InMemoryAccountDao accountDao;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountDao = new InMemoryAccountDao();
        accountService = new AccountServiceImpl(accountDao);
    }

    @Test
    void saveEncryptsPasswordAndPreservesConfigurationMetadata() {
        Account plainAccount = account(
                "sender@gmail.com",
                "plain-app-password",
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
        plainAccount.setId(42L);

        Account saved = accountService.save(plainAccount);

        assertAll(
                () -> assertSame(accountDao.savedAccount, saved),
                () -> assertNotEquals(plainAccount.getPassword(), saved.getPassword()),
                () -> assertTrue(saved.getPassword().startsWith("v2:")),
                () -> assertEquals(plainAccount.getPassword(), CryptoUtil.decrypt(saved.getPassword())),
                () -> assertEquals(42L, saved.getId()),
                () -> assertEquals("Sender", saved.getDisplayName()),
                () -> assertTrue(saved.isPrimary()),
                () -> assertSame(plainAccount.getMailServerConfig(), saved.getMailServerConfig()),
                () -> assertEquals("plain-app-password", plainAccount.getPassword())
        );
    }

    @Test
    void findAllDecryptsStoredPasswordsAndPreservesConfigurationMetadata() {
        Account stored = account(
                "sender@gmail.com",
                CryptoUtil.encrypt("decoded-app-password"),
                "Sender",
                true,
                new MailServerConfig(
                        "smtp.example.com",
                        465,
                        "imap.example.com",
                        993,
                        SecurityMode.SSL,
                        SecurityMode.SSL
                )
        );
        stored.setId(7L);
        accountDao.accounts.add(stored);

        Account loaded = accountService.findAll().getFirst();

        assertAll(
                () -> assertEquals("decoded-app-password", loaded.getPassword()),
                () -> assertFalse(loaded.isPasswordDecryptionFailed()),
                () -> assertEquals(7L, loaded.getId()),
                () -> assertEquals("Sender", loaded.getDisplayName()),
                () -> assertTrue(loaded.isPrimary()),
                () -> assertSame(stored.getMailServerConfig(), loaded.getMailServerConfig()),
                () -> assertNotEquals(stored.getPassword(), loaded.getPassword())
        );
    }

    @Test
    void findAllMarksAccountWhenStoredPasswordCannotBeDecrypted() {
        Account stored = account(
                "sender@gmail.com",
                "not-valid-encrypted-content",
                "Sender",
                true,
                new MailServerConfig()
        );
        accountDao.accounts.add(stored);

        Account loaded = accountService.findAll().getFirst();

        assertAll(
                () -> assertEquals("", loaded.getPassword()),
                () -> assertTrue(loaded.isPasswordDecryptionFailed()),
                () -> assertEquals("sender@gmail.com", loaded.getUsername())
        );
    }

    @Test
    void findPrimaryConfigurationReturnsFirstDaoAccountOrNull() {
        assertNull(accountService.findPrimaryConfiguration());

        accountDao.accounts.add(account("primary@gmail.com", CryptoUtil.encrypt("one"), "Primary", true, new MailServerConfig()));
        accountDao.accounts.add(account("secondary@gmail.com", CryptoUtil.encrypt("two"), "Secondary", false, new MailServerConfig()));

        Account primary = accountService.findPrimaryConfiguration();

        assertAll(
                () -> assertNotNull(primary),
                () -> assertEquals("primary@gmail.com", primary.getUsername()),
                () -> assertEquals("one", primary.getPassword())
        );
    }

    @Test
    void findByEmailDecryptsFoundAccountAndHandlesMissingInput() {
        accountDao.accounts.add(account(
                "sender@gmail.com",
                CryptoUtil.encrypt("app-password"),
                "Sender",
                true,
                new MailServerConfig()
        ));

        Account found = accountService.findByEmailAddress("SENDER@gmail.com");

        assertAll(
                () -> assertNotNull(found),
                () -> assertEquals("app-password", found.getPassword()),
                () -> assertNull(accountService.findByEmailAddress(null)),
                () -> assertNull(accountService.findByEmailAddress("missing@gmail.com"))
        );
    }

    @Test
    void updateEncryptsPasswordBeforeDelegatingToDao() {
        Account plainAccount = account(
                "sender@gmail.com",
                "new-app-password",
                "Sender",
                true,
                new MailServerConfig()
        );

        accountService.update(plainAccount);

        assertAll(
                () -> assertNotNull(accountDao.updatedAccount),
                () -> assertNotEquals("new-app-password", accountDao.updatedAccount.getPassword()),
                () -> assertEquals("new-app-password", CryptoUtil.decrypt(accountDao.updatedAccount.getPassword())),
                () -> assertEquals("new-app-password", plainAccount.getPassword())
        );
    }

    @Test
    void updateAndDeleteIgnoreMissingIdentity() {
        accountService.update(null);
        accountService.update(new Account("", "password", LocalDateTime.now()));
        accountService.deleteByEmailAddress(null);
        accountService.deleteByEmailAddress(" ");

        assertAll(
                () -> assertNull(accountDao.updatedAccount),
                () -> assertNull(accountDao.deletedEmail)
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

    private static final class InMemoryAccountDao implements AccountDao {
        private final List<Account> accounts = new ArrayList<>();
        private Account savedAccount;
        private Account updatedAccount;
        private String deletedEmail;

        @Override
        public Account save(Account account) {
            savedAccount = account;
            return account;
        }

        @Override
        public List<Account> findAll() {
            return List.copyOf(accounts);
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return accounts.stream()
                    .filter(account -> account.getUsername().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public void update(Account account) {
            updatedAccount = account;
        }

        @Override
        public void deleteByEmail(String email) {
            deletedEmail = email;
        }
    }
}
