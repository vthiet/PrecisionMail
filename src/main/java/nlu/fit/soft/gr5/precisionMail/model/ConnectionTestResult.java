package nlu.fit.soft.gr5.precisionMail.model;

/**
 * Kết quả kiểm tra kết nối SMTP/IMAP của UC-01.
 *
 * <p>Commit UC-01 #17 - Anh Han: thay exception trực tiếp bằng model kết quả
 * an toàn để controller biết loại lỗi, bước lỗi và nguyên nhân kỹ thuật.</p>
 *
 * @param type loại kết quả kiểm tra kết nối
 * @param step bước SMTP/IMAP xảy ra lỗi hoặc hoàn tất
 * @param message thông điệp kỹ thuật ngắn gọn cho logging/debug
 * @param cause exception gốc nếu kiểm tra thất bại
 * @author Anh Han
 */
public record ConnectionTestResult(Type type, Step step, String message, Throwable cause) {

    /**
     * Loại kết quả hoặc nhóm lỗi khi kiểm tra mail server.
     */
    public enum Type {
        SUCCESS,
        AUTH_FAILED,
        TIMEOUT,
        SMTP_FAILED,
        IMAP_FAILED,
        UNKNOWN_FAILED
    }

    /**
     * Bước kiểm tra kết nối đang thực hiện hoặc nơi phát sinh lỗi.
     */
    public enum Step {
        SMTP,
        IMAP,
        UNKNOWN
    }

    /**
     * Tạo kết quả khi chưa xác định được bước lỗi cụ thể.
     *
     * @param type loại kết quả
     * @param message thông điệp kỹ thuật
     * @param cause exception gốc
     */
    public ConnectionTestResult(Type type, String message, Throwable cause) {
        this(type, Step.UNKNOWN, message, cause);
    }

    /**
     * Kiểm tra kết quả có phải thành công hay không.
     *
     * @return true nếu SMTP và IMAP đều kiểm tra thành công
     */
    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }

    /**
     * Tạo kết quả thành công sau khi kiểm tra đủ SMTP và IMAP.
     *
     * @return kết quả thành công
     */
    public static ConnectionTestResult success() {
        return new ConnectionTestResult(Type.SUCCESS, Step.IMAP, "Connection test completed successfully.", null);
    }

    /**
     * Tạo kết quả thất bại khi chưa có thông tin bước lỗi.
     *
     * @param type loại lỗi
     * @param message thông điệp kỹ thuật
     * @param cause exception gốc
     * @return kết quả thất bại
     */
    public static ConnectionTestResult failure(Type type, String message, Throwable cause) {
        return new ConnectionTestResult(type, Step.UNKNOWN, message, cause);
    }

    /**
     * Tạo kết quả thất bại có kèm bước SMTP/IMAP phát sinh lỗi.
     *
     * @param type loại lỗi
     * @param step bước phát sinh lỗi
     * @param message thông điệp kỹ thuật
     * @param cause exception gốc
     * @return kết quả thất bại
     */
    public static ConnectionTestResult failure(Type type, Step step, String message, Throwable cause) {
        return new ConnectionTestResult(type, step, message, cause);
    }
}
