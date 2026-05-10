package nlu.fit.soft.gr5.precisionMail.dao;

import nlu.fit.soft.gr5.precisionMail.model.Account;

import java.util.List;

public interface AccountDao {

    Account save(Account account);

    List<Account> findAll();
}
