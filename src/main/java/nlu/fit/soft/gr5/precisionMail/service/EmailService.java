package nlu.fit.soft.gr5.precisionMail.service;

import jakarta.mail.MessagingException;

import java.util.List;

public interface EmailService {

    void send(String to, String subject, String content) throws MessagingException;

    void send(List<String> to,
              List<String> cc,
              List<String> bcc,
              String subject,
              String content) throws MessagingException;
}
