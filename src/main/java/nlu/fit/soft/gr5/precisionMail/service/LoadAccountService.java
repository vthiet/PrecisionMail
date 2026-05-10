package nlu.fit.soft.gr5.precisionMail.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;

import java.util.List;

public class LoadAccountService  extends Service<List<Account>> {
    private final AccountService accountService = new AccountServiceImpl();

    @Override
    protected Task<List<Account>> createTask() {
        return new Task<List<Account>>() {
            @Override
            protected List<Account> call() throws Exception {
                Thread.sleep(1500);
                return accountService.findAll();
            }
        };
    }
}
