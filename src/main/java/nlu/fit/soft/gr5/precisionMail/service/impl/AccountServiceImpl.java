package nlu.fit.soft.gr5.precisionMail.service.impl;

import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;

import java.util.List;

public class AccountServiceImpl implements AccountService {

    @Override
    public List<Account> findAll() {
        return List.of(new Account("thietvo02@gmail.come", "sadjflkajdlkfaj"),
                new Account("thietvo03@gmail.come", "sadjflkajdlkfaj"),
                new Account("thietvoasd02@gmail.come", "sadjflkajdlkfaj"));
    }
}
