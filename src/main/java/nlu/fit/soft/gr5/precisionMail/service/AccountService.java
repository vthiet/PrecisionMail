package nlu.fit.soft.gr5.precisionMail.service;

import nlu.fit.soft.gr5.precisionMail.model.Account;

import java.util.List;

public interface AccountService {

    Account save(String username, String password);

    List<Account> findAll();

    Account findByEmailAddress(String emailAddress);

    void update(Account account);

    void deleteByEmailAddress(String emailAddress);
}
