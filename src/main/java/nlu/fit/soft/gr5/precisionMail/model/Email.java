package nlu.fit.soft.gr5.precisionMail.model;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class Email {

    public Long id;
    public String from;
    public Set<String> toLst;
    public Set<String> cc;
    public Set<String> bcc;
    public String subject;
    public String content;
    public List<File> attachments;
    public LocalDateTime sentAt;

    public Email() { }

    public Email(String from,
                 Set<String> toLst,
                 Set<String> ccLst,
                 Set<String> bccLst,
                 String subject,
                 String content,
                 List<File> attachments,
                 LocalDateTime sentAt
    ){
        this.from = from;
        this.toLst = toLst;
        this.cc = ccLst;
        this.bcc = bccLst;
        this.subject = subject;
        this.content = content;
        this.attachments = attachments;
        this.sentAt = sentAt;
    }
}
