package nlu.fit.soft.gr5.precisionMail.model;

import java.time.LocalDateTime;

public class Account {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;

    public Account(String username, String password, LocalDateTime createdAt){
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }
}
