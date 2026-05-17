package nlu.fit.soft.gr5.precisionMail.model;

import java.time.LocalDateTime;

public class Account {
    private Long id;
    private String username;
    private String password;
    private MailServerConfig mailServerConfig;
    private LocalDateTime createdAt;

    public Account() { }

    public Account(String username, String password, LocalDateTime createdAt){
        this.username = username;
        this.password = password;
        this.mailServerConfig = new MailServerConfig();
        this.createdAt = createdAt;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public MailServerConfig getMailServerConfig() {
        if (mailServerConfig == null) {
            mailServerConfig = new MailServerConfig();
        }
        return mailServerConfig;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setMailServerConfig(MailServerConfig mailServerConfig) {
        this.mailServerConfig = mailServerConfig;
    }
}
