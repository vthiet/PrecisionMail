package nlu.fit.soft.gr5.precisionMail.model;

public class MailServerConfig {
    private String smtpHost;
    private int smtpPort;
    private String imapHost;
    private int imapPort;
    private SecurityMode smtpSecurityMode;
    private SecurityMode imapSecurityMode;

    public MailServerConfig() {
        this("smtp.gmail.com", 587, "imap.gmail.com", 993, SecurityMode.TLS, SecurityMode.SSL);
    }

    public MailServerConfig(String smtpHost, int smtpPort, String imapHost, int imapPort, SecurityMode securityMode) {
        this(smtpHost, smtpPort, imapHost, imapPort, securityMode, securityMode);
    }

    public MailServerConfig(
            String smtpHost,
            int smtpPort,
            String imapHost,
            int imapPort,
            SecurityMode smtpSecurityMode,
            SecurityMode imapSecurityMode
    ) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.imapHost = imapHost;
        this.imapPort = imapPort;
        this.smtpSecurityMode = smtpSecurityMode == null ? SecurityMode.TLS : smtpSecurityMode;
        this.imapSecurityMode = imapSecurityMode == null ? SecurityMode.SSL : imapSecurityMode;
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
        return smtpSecurityMode;
    }

    public SecurityMode getSmtpSecurityMode() {
        return smtpSecurityMode;
    }

    public SecurityMode getImapSecurityMode() {
        return imapSecurityMode;
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
        setSmtpSecurityMode(securityMode);
        setImapSecurityMode(securityMode);
    }

    public void setSmtpSecurityMode(SecurityMode smtpSecurityMode) {
        this.smtpSecurityMode = smtpSecurityMode == null ? SecurityMode.TLS : smtpSecurityMode;
    }

    public void setImapSecurityMode(SecurityMode imapSecurityMode) {
        this.imapSecurityMode = imapSecurityMode == null ? SecurityMode.SSL : imapSecurityMode;
    }
}
