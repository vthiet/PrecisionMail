package nlu.fit.soft.gr5.precisionMail.model;

public class Account {
    private String username;
    private String password;

    public Account(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }
}
