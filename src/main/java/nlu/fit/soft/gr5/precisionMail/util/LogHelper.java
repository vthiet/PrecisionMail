package nlu.fit.soft.gr5.precisionMail.util;

import nlu.fit.soft.gr5.precisionMail.model.Email;

/**
 * Utility hỗ trợ ghi log an toàn cho các use case gửi mail.
 *
 * <p>Lớp này cung cấp các hàm mask email, đếm người nhận và đếm attachment
 * để log chỉ chứa metadata cần thiết, không ghi trực tiếp dữ liệu nhạy cảm.</p>
 *
 * <p>Commit UC-01 #5 - Anh Han: bổ sung masking email cho log cấu hình SMTP/IMAP.</p>
 */
public final class LogHelper {
    private LogHelper() {
    }

    /**
     * Che một phần địa chỉ email trước khi ghi log.
     *
     * <p>Ví dụ: {@code sender@gmail.com} thành {@code sen***@gmail.com}.</p>
     *
     * @param email email cần che
     * @return email đã được che, hoặc {@code unknown} nếu đầu vào rỗng
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String visibleLocalPart = localPart.substring(0, Math.min(3, localPart.length()));
        return visibleLocalPart + "***" + email.substring(atIndex);
    }

    /**
     * Đếm tổng số người nhận trong To, Cc và Bcc.
     *
     * @param email email cần đếm người nhận
     * @return tổng số người nhận, null được tính là 0
     */
    public static int recipientCount(Email email) {
        return sizeOf(email.toLst) + sizeOf(email.cc) + sizeOf(email.bcc);
    }

    /**
     * Đếm số attachment của email.
     *
     * @param email email cần đếm attachment
     * @return số attachment, null được tính là 0
     */
    public static int attachmentCount(Email email) {
        return email.attachments == null ? 0 : email.attachments.size();
    }

    private static int sizeOf(java.util.Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }
}
