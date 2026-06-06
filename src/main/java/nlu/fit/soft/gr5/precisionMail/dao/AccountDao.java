package nlu.fit.soft.gr5.precisionMail.dao;

import nlu.fit.soft.gr5.precisionMail.model.Account;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface AccountDao {

    /**
     *
     * @param account
     * @return account object
     */
    Account save(Account account);

    List<Account> findAll();

    Optional<Account> findByEmail(String email);

    void update(Account account);

    void deleteByEmail(String email);
}
