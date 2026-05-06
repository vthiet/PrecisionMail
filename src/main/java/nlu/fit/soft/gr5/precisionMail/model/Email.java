package nlu.fit.soft.gr5.precisionMail.model;

import java.time.LocalDateTime;

public class Email {

    private Long id;
    private String fromEmail;
    private String toEmail;
    private String subject;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
