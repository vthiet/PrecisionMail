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

    // Pre-condition: Cung cấp danh sách chuỗi email tài khoản có sẵn nhằm phục vụ cho dropdown người gửi
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
                    return decrypted;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Account findByEmailAddress(String emailAddress) {
        return null;
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
        } catch (IllegalStateException ex) {
            LOGGER.warn("Stored password could not be decrypted, falling back to raw value for compatibility.");
            return encryptedPassword;
        }
    }
}
