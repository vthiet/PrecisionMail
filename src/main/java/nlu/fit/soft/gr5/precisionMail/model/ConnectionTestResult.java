package nlu.fit.soft.gr5.precisionMail.model;

public record ConnectionTestResult(Type type, String message, Throwable cause) {

    public enum Type {
        SUCCESS,
        AUTH_FAILED,
        TIMEOUT,
        SMTP_FAILED,
        IMAP_FAILED,
        UNKNOWN_FAILED
    }

    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }

    public static ConnectionTestResult success() {
        return new ConnectionTestResult(Type.SUCCESS, "Connection test completed successfully.", null);
    }

    public static ConnectionTestResult failure(Type type, String message, Throwable cause) {
        return new ConnectionTestResult(type, message, cause);
    }
}
