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

    ConnectionTestResult validateConnection(Account account);

    ConnectionTestResult validateConnection(Account account, Consumer<ConnectionTestProgress> progressListener);

    List<Email> findAll() throws IOException;

    Email save(Email email) throws IOException;

    record SendResult(Email email, boolean success, Throwable error) {
    }
}
