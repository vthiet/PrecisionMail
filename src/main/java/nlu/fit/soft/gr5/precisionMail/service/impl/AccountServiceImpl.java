package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.dao.AccountDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.AccountDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;

import java.time.LocalDateTime;
import java.util.List;

public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao = new AccountDaoImpl();

    @Override
    public Account save(String username, String password) {
        Account account = new Account(username, password, LocalDateTime.now());
        return accountDao.save(account);
    }

    @Override
    public List<Account> findAll() {
        return accountDao.findAll();
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
}
