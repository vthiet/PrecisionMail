package nlu.fit.soft.gr5.precisionMail.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// Pre-condition: Tải danh sách tài khoản đã lưu trữ từ cơ sở dữ liệu/file hệ thống lên ứng dụng
public class LoadAccountService  extends Service<List<Account>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadAccountService.class);
    private final AccountService accountService = new AccountServiceImpl();

    @Override
    protected Task<List<Account>> createTask() {
        return new Task<List<Account>>() {
            @Override
            protected List<Account> call() throws Exception {
                LOGGER.debug("Background account loading started.");
                // Tìm kiếm tất cả tài khoản hợp lệ đang được cấu hình trong hệ thống
                List<Account> accounts = accountService.findAll();
                LOGGER.debug("Background account loading completed. accountCount={}", accounts.size());
                return accounts;
            }
        };
    }
}
