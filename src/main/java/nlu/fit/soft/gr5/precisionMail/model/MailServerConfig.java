package nlu.fit.soft.gr5.precisionMail.model;

public class MailServerConfig {
    private String smtpHost;
    private int smtpPort;
    private String imapHost;
    private int imapPort;
    private SecurityMode securityMode;

    public MailServerConfig() {
        this("smtp.gmail.com", 587, "imap.gmail.com", 993, SecurityMode.TLS);
    }

    public MailServerConfig(String smtpHost, int smtpPort, String imapHost, int imapPort, SecurityMode securityMode) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.imapHost = imapHost;
        this.imapPort = imapPort;
        this.securityMode = securityMode;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getImapHost() {
        return imapHost;
    }

    public int getImapPort() {
        return imapPort;
    }

    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public void setImapHost(String imapHost) {
        this.imapHost = imapHost;
    }

    public void setImapPort(int imapPort) {
        this.imapPort = imapPort;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
    }
}
