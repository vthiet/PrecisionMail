package nlu.fit.soft.gr5.precisionMail.util;

/**
 * Validator cho dữ liệu cấu hình mail server của UC-01.
 *
 * <p>Commit UC-01 #3 - Anh Han: tách kiểm tra port để có unit test rõ ràng
 * cho các giá trị biên 1, 65535 và input sai định dạng.</p>
 */
public final class MailServerConfigValidator {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final int INVALID_PORT = -1;

    private MailServerConfigValidator() {
    }

    /**
     * Kiểm tra chuỗi port có nằm trong miền hợp lệ 1..65535 hay không.
     *
     * @param value giá trị port nhập từ UI
     * @return true nếu port hợp lệ
     */
    public static boolean isValidPort(String value) {
        int port = parsePort(value);
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    /**
     * Parse port từ chuỗi nhập vào form.
     *
     * @param value giá trị port nhập từ UI
     * @return port dạng số, hoặc -1 nếu không parse được
     */
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
