package nlu.fit.soft.gr5.precisionMail.util;

/**
 * Utility làm sạch dữ liệu nhạy cảm trước khi log được hiển thị hoặc ghi ra layout.
 *
 * <p>Lớp này che email và các trường có khả năng chứa password/token/secret.
 * Đây là lớp bảo vệ chính cho yêu cầu không lộ thông tin nhạy cảm trong UC-07.</p>
 */
public final class LogSanitizer {
    private static final String EMAIL_REGEX = "([A-Za-z0-9._%+-]{1,3})[A-Za-z0-9._%+-]*(@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})";
    private static final String PASSWORD_REGEX = "(?i)(password|app password|appPassword|pass|token|secret)(\\s*[=:]\\s*)\"?[^\",\\s]+\"?";

    private LogSanitizer() {
    }

    /**
     * Che dữ liệu nhạy cảm trong một chuỗi log.
     *
     * @param value chuỗi log đầu vào
     * @return chuỗi đã được che email/password/token/secret; giữ nguyên null hoặc blank
     */
    public static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value
                .replaceAll(PASSWORD_REGEX, "$1$2[PROTECTED_PASSWORD]")
                .replaceAll(EMAIL_REGEX, "$1***$2");
    }
}
