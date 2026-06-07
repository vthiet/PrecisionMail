package nlu.fit.soft.gr5.precisionMail.service;

import jakarta.mail.MessagingException;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.ConnectionTestProgress;
import nlu.fit.soft.gr5.precisionMail.model.ConnectionTestResult;
import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface EmailService {

    void send(Account account, Email email) throws MessagingException, IOException;

    CompletableFuture<SendResult> sendAsync(Account account, Email email);

    /**
     * Kiểm tra kết nối SMTP và IMAP của tài khoản email.
     *
     * @param account tài khoản email cần kiểm tra
     * @return kết quả kiểm tra kết nối
     */
    ConnectionTestResult validateConnection(Account account);

    /**
     * Kiểm tra kết nối SMTP/IMAP và phát trạng thái tiến trình cho UI.
     *
     * <p>Commit UC-01 #18 - Anh Han: cho phép dialog cấu hình hiển thị rõ
     * đang test SMTP hay IMAP.</p>
     *
     * @param account tài khoản email cần kiểm tra
     * @param progressListener callback nhận trạng thái tiến trình, có thể null
     * @return kết quả kiểm tra kết nối
     */
    ConnectionTestResult validateConnection(Account account, Consumer<ConnectionTestProgress> progressListener);

    List<Email> findAll() throws IOException;

    Email save(Email email) throws IOException;

    record SendResult(Email email, boolean success, Throwable error) {
    }
}
