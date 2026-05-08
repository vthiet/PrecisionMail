package nlu.fit.soft.gr5.precisionMail.model;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class Email {

    protected Long id;
    protected String from;
    protected List<String> to;
    protected List<String> cc;
    protected List<String> bcc;
    protected String subject;
    protected String content;
    protected List<File> attachments;
    protected LocalDateTime createdAt;
    protected LocalDateTime sentAt;
}
