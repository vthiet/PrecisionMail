package nlu.fit.soft.gr5.precisionMail.util;

public final class MailServerConfigValidator {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final int INVALID_PORT = -1;

    private MailServerConfigValidator() {
    }

    public static boolean isValidPort(String value) {
        int port = parsePort(value);
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    public static int parsePort(String value) {
        if (value == null) {
            return INVALID_PORT;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return INVALID_PORT;
        }
    }
}
