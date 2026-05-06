package nlu.fit.soft.gr5.precisionMail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

public interface EmailService {

    void send(String to, String subject, String content) throws MessagingException;
}
