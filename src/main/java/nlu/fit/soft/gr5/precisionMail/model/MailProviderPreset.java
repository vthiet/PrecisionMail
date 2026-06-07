package nlu.fit.soft.gr5.precisionMail.model;

/**
 * Preset cấu hình SMTP/IMAP cho các nhà cung cấp email phổ biến trong UC-01.
 *
 * <p>Commit UC-01 #8-#9 - Anh Han: hỗ trợ Gmail, Outlook, Yahoo và Custom
 * để người dùng vừa có thể tự điền nhanh, vừa có thể cấu hình thủ công.</p>
 *
 * @author Anh Han
 */
public enum MailProviderPreset {
    CUSTOM("Custom", null),
    GMAIL("Gmail", new MailServerConfig("smtp.gmail.com", 587, "imap.gmail.com", 993, SecurityMode.TLS, SecurityMode.SSL)),
    OUTLOOK("Outlook", new MailServerConfig("smtp.office365.com", 587, "outlook.office365.com", 993, SecurityMode.TLS, SecurityMode.SSL)),
    YAHOO("Yahoo", new MailServerConfig("smtp.mail.yahoo.com", 465, "imap.mail.yahoo.com", 993, SecurityMode.SSL, SecurityMode.SSL));

    private final String displayName;
    private final MailServerConfig config;

    MailProviderPreset(String displayName, MailServerConfig config) {
        this.displayName = displayName;
        this.config = config;
    }

    /**
     * Kiểm tra preset có cấu hình SMTP/IMAP cố định hay không.
     *
     * @return true với Gmail/Outlook/Yahoo, false với Custom
     */
    public boolean hasConfig() {
        return config != null;
    }

    /**
     * Trả về bản sao cấu hình của provider để tránh sửa trực tiếp preset gốc.
     *
     * @return cấu hình SMTP/IMAP của provider, hoặc null với Custom
     */
    public MailServerConfig getConfig() {
        if (config == null) {
            return null;
        }
        return new MailServerConfig(
                config.getSmtpHost(),
                config.getSmtpPort(),
                config.getImapHost(),
                config.getImapPort(),
                config.getSmtpSecurityMode(),
                config.getImapSecurityMode()
        );
    }

    /**
     * Nhận diện provider từ cấu hình đã lưu.
     *
     * @param config cấu hình SMTP/IMAP cần so khớp
     * @return preset tương ứng, hoặc Custom nếu không trùng provider chuẩn
     */
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
                && first.getSmtpSecurityMode() == second.getSmtpSecurityMode()
                && first.getImapSecurityMode() == second.getImapSecurityMode();
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
