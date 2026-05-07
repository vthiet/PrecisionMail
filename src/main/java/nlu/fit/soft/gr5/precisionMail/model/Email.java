package nlu.fit.soft.gr5.precisionMail.model;

import java.time.LocalDateTime;
import java.util.List;

public class Email {

    private Long id;
    private String from;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
