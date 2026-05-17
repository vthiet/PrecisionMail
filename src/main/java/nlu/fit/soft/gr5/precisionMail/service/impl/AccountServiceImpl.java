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

    private final AccountDao accountDao = new AccountDaoImpl();

    @Override
    public Account save(String username, String password) {
        LOGGER.info("Account save requested for username={}.", LogHelper.maskEmail(username));
        Account account = new Account(username, CryptoUtil.encrypt(password), LocalDateTime.now());
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
        if (account.getId() != null) {
            encrypted.setId(account.getId());
        }
        return accountDao.save(encrypted);
    }

    @Override
    public List<Account> findAll() {
        LOGGER.debug("Account list load requested.");
        List<Account> savedAccounts = accountDao.findAll();
        return savedAccounts.stream()
                .map(account -> {
                    Account decrypted = new Account(
                            account.getUsername(),
                            decryptPassword(account.getPassword()),
                            account.getCreatedAt()
                    );
                    if (account.getId() != null) {
                        decrypted.setId(account.getId());
                    }
                    decrypted.setMailServerConfig(account.getMailServerConfig());
                    return decrypted;
                })
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
        return findAll().stream()
                .filter(account -> emailAddress.equalsIgnoreCase(account.getUsername()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(Account account) {

    }

    @Override
    public void deleteByEmailAddress(String emailAddress) {

    }

    private String decryptPassword(String encryptedPassword) {
        try {
            return CryptoUtil.decrypt(encryptedPassword);
        } catch (RuntimeException | ExceptionInInitializerError ex) {
            LOGGER.warn("Stored password could not be decrypted, falling back to raw value for compatibility.");
            return encryptedPassword;
        }
    }
}
