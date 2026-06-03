package nlu.fit.soft.gr5.precisionMail.model;

public enum MailProviderPreset {
    CUSTOM("Custom", null),
    GMAIL("Gmail", new MailServerConfig("smtp.gmail.com", 587, "imap.gmail.com", 993, SecurityMode.TLS)),
    OUTLOOK("Outlook", new MailServerConfig("smtp.office365.com", 587, "outlook.office365.com", 993, SecurityMode.TLS)),
    YAHOO("Yahoo", new MailServerConfig("smtp.mail.yahoo.com", 465, "imap.mail.yahoo.com", 993, SecurityMode.SSL));

    private final String displayName;
    private final MailServerConfig config;

    MailProviderPreset(String displayName, MailServerConfig config) {
        this.displayName = displayName;
        this.config = config;
    }

    public boolean hasConfig() {
        return config != null;
    }

    public MailServerConfig getConfig() {
        if (config == null) {
            return null;
        }
        return new MailServerConfig(
                config.getSmtpHost(),
                config.getSmtpPort(),
                config.getImapHost(),
                config.getImapPort(),
                config.getSecurityMode()
        );
    }

    public static MailProviderPreset inferFrom(MailServerConfig config) {
        if (config == null) {
            return CUSTOM;
        }

        for (MailProviderPreset preset : values()) {
            if (!preset.hasConfig()) {
                continue;
            }
            if (sameConfig(config, preset.config)) {
                return preset;
            }
        }
        return CUSTOM;
    }

    private static boolean sameConfig(MailServerConfig first, MailServerConfig second) {
        return equalsIgnoreCase(first.getSmtpHost(), second.getSmtpHost())
                && first.getSmtpPort() == second.getSmtpPort()
                && equalsIgnoreCase(first.getImapHost(), second.getImapHost())
                && first.getImapPort() == second.getImapPort()
                && first.getSecurityMode() == second.getSecurityMode();
    }

    private static boolean equalsIgnoreCase(String first, String second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.equalsIgnoreCase(second);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
