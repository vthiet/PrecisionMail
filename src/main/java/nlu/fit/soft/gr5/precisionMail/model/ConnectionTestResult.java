package nlu.fit.soft.gr5.precisionMail.model;

public record ConnectionTestResult(Type type, Step step, String message, Throwable cause) {

    public enum Type {
        SUCCESS,
        AUTH_FAILED,
        TIMEOUT,
        SMTP_FAILED,
        IMAP_FAILED,
        UNKNOWN_FAILED
    }

    public enum Step {
        SMTP,
        IMAP,
        UNKNOWN
    }

    public ConnectionTestResult(Type type, String message, Throwable cause) {
        this(type, Step.UNKNOWN, message, cause);
    }

    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }

    public static ConnectionTestResult success() {
        return new ConnectionTestResult(Type.SUCCESS, Step.IMAP, "Connection test completed successfully.", null);
    }

    public static ConnectionTestResult failure(Type type, String message, Throwable cause) {
        return new ConnectionTestResult(type, Step.UNKNOWN, message, cause);
    }

    public static ConnectionTestResult failure(Type type, Step step, String message, Throwable cause) {
        return new ConnectionTestResult(type, step, message, cause);
    }
}
