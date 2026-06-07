package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.dao.AccountDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.AccountDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;
import nlu.fit.soft.gr5.precisionMail.util.CryptoUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountDao accountDao;

    public AccountServiceImpl() {
        this(new AccountDaoImpl());
    }

    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Account save(String username, String password) {
        LOGGER.info("Account save requested for username={}.", LogHelper.maskEmail(username));
        Account account = new Account(username, CryptoUtil.encrypt(password), LocalDateTime.now());
        account.setDisplayName(username);
        account.setPrimary(true);
        return accountDao.save(account);
    }

    @Override
    public Account save(Account account) {
        LOGGER.info("Account configuration save requested for username={}.", LogHelper.maskEmail(account.getUsername()));
        Account encrypted = new Account(
                account.getUsername(),
                CryptoUtil.encrypt(account.getPassword()),
                account.getCreatedAt() == null ? LocalDateTime.now() : account.getCreatedAt()
        );
        encrypted.setMailServerConfig(account.getMailServerConfig());
        copyAccountMetadata(account, encrypted);
        encrypted.setPasswordDecryptionFailed(false);
        return accountDao.save(encrypted);
    }

    @Override
    public List<Account> findAll() {
        LOGGER.debug("Account list load requested.");
        List<Account> savedAccounts = accountDao.findAll();
        return savedAccounts.stream()
                .map(this::toDecryptedAccount)
                .collect(Collectors.toList());
    }

    @Override
    public Account findPrimaryConfiguration() {
        return findAll().stream().findFirst().orElse(null);
    }

    @Override
    public Account findByEmailAddress(String emailAddress) {
        if (emailAddress == null) {
            return null;
        }
        return accountDao.findByEmail(emailAddress)
                .map(this::toDecryptedAccount)
                .orElse(null);
    }

    @Override
    public void update(Account account) {
        if (account == null || account.getUsername() == null || account.getUsername().isBlank()) {
            return;
        }
        LOGGER.info("Account update requested for username={}.", LogHelper.maskEmail(account.getUsername()));
        Account encrypted = new Account(
                account.getUsername(),
                CryptoUtil.encrypt(account.getPassword()),
                account.getCreatedAt() == null ? LocalDateTime.now() : account.getCreatedAt()
        );
        encrypted.setMailServerConfig(account.getMailServerConfig());
        copyAccountMetadata(account, encrypted);
        encrypted.setPasswordDecryptionFailed(false);
        accountDao.update(encrypted);
    }

    @Override
    public void deleteByEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            return;
        }
        LOGGER.info("Account delete requested for username={}.", LogHelper.maskEmail(emailAddress));
        accountDao.deleteByEmail(emailAddress);
    }

    private Account toDecryptedAccount(Account account) {
        PasswordDecodeResult password = decryptPassword(account);
        Account decrypted = new Account(
                account.getUsername(),
                password.value(),
                account.getCreatedAt()
        );
        decrypted.setMailServerConfig(account.getMailServerConfig());
        copyAccountMetadata(account, decrypted);
        decrypted.setPasswordDecryptionFailed(password.failed());
        return decrypted;
    }

    private PasswordDecodeResult decryptPassword(Account account) {
        try {
            return new PasswordDecodeResult(CryptoUtil.decrypt(account.getPassword()), false);
        } catch (RuntimeException | ExceptionInInitializerError ex) {
            LOGGER.warn(
                    "Stored app password could not be decrypted for username={}. User must re-enter it.",
                    LogHelper.maskEmail(account.getUsername())
            );
            return new PasswordDecodeResult("", true);
        }
    }

    private void copyAccountMetadata(Account source, Account target) {
        if (source.getId() != null) {
            target.setId(source.getId());
        }
        target.setDisplayName(source.getDisplayName());
        target.setPrimary(source.isPrimary());
        target.setPasswordDecryptionFailed(source.isPasswordDecryptionFailed());
    }

    private record PasswordDecodeResult(String value, boolean failed) {
    }
}
