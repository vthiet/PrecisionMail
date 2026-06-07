package nlu.fit.soft.gr5.precisionMail.util;

import java.util.regex.Pattern;

public final class LogSanitizer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "([A-Za-z0-9._%+-]{1,3})[A-Za-z0-9._%+-]*(@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})"
    );
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?i)(token|access[ _-]?key|api[ _-]?key)(\\s*[=:]\\s*)\"?[^\",\\s]+\"?"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(?i)(password|app[ _-]?password|pass|secret)(\\s*[=:]\\s*)\"?[^\",\\s]+\"?"
    );

    private LogSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        // BR-06-01 / BF-6.1.5: mask credentials before log content reaches the UI or log file.
        String tokenProtected = TOKEN_PATTERN.matcher(value).replaceAll("$1$2[PROTECTED_TOKEN]");
        String passwordProtected = PASSWORD_PATTERN.matcher(tokenProtected).replaceAll("$1$2[PROTECTED_PASSWORD]");
        return EMAIL_PATTERN.matcher(passwordProtected).replaceAll("$1***$2");
    }
}
