package nlu.fit.soft.gr5.precisionMail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;

import java.io.IOException;
import java.util.List;

public interface EmailService {

    void send(Account account, Email email) throws MessagingException, IOException;

    List<Email> findAll() throws IOException;

    Email save(Email email) throws IOException;
}
