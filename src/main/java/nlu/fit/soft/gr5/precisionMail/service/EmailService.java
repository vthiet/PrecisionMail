package nlu.fit.soft.gr5.precisionMail.service;

import jakarta.mail.MessagingException;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EmailService {

    void send(Account account, Email email) throws MessagingException, IOException;

    CompletableFuture<SendResult> sendAsync(Account account, Email email);

    void validateConnection(Account account) throws MessagingException;

    List<Email> findAll() throws IOException;

    Email save(Email email) throws IOException;

    record SendResult(Email email, boolean success, Throwable error) {
    }
}
