package nlu.fit.soft.gr5.precisionMail.util;

public final class LogSanitizer {
    private static final String EMAIL_REGEX = "([A-Za-z0-9._%+-]{1,3})[A-Za-z0-9._%+-]*(@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})";
    private static final String PASSWORD_REGEX = "(?i)(password|app password|token|secret)(\\s*[=:]\\s*)\\S+";

    private LogSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value
                .replaceAll(PASSWORD_REGEX, "$1$2[PROTECTED_PASSWORD]")
                .replaceAll(EMAIL_REGEX, "$1***$2");
    }
}
