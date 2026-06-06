package nlu.fit.soft.gr5.precisionMail.model;

import java.time.LocalDateTime;

/**
 * Thực thể DraftEmail dùng để tự động lưu các bản nháp email đang soạn thảo.
 * Hệ thống sẽ tự động lưu mỗi 30 giây một lần để tránh mất mát dữ liệu của người dùng.
 */
public class DraftEmail {
    // Mã định danh duy nhất (Primary Key) của bản nháp
    public Long id;

    // Địa chỉ email của người gửi (Tài khoản đang đăng nhập)
    public String senderEmail;

    // Danh sách email người nhận (phân tách bằng dấu phẩy hoặc xuống dòng)
    public String toRecipients;

    // Danh sách email đồng gửi - Cc (Carbon Copy)
    public String ccRecipients;

    // Danh sách email ẩn danh - Bcc (Blind Carbon Copy)
    public String bccRecipients;

    // Tiêu đề của email nháp
    public String subject;

    // Nội dung của email (định dạng HTML)
    public String body;

    // Đường dẫn các tệp đính kèm (phân tách bằng dấu xuống dòng)
    public String attachmentPaths;

    // Thời điểm cuối cùng bản nháp này được hệ thống tự động lưu
    public LocalDateTime lastSavedAt;

    // Thời điểm bản nháp được tạo ra lần đầu tiên
    public LocalDateTime createdAt;

    // Thời điểm bản nháp được cập nhật gần nhất
    public LocalDateTime updatedAt;

    /**
     * Constructor khởi tạo mặc định (Không tham số)
     * Thường được sử dụng bởi các Framework ORM (như Hibernate/JPA)
     */
    public DraftEmail() {}

    /**
     * Constructor khởi tạo đầy đủ tham số cơ bản khi người dùng bắt đầu soạn email.
     * Các mốc thời gian (createdAt, updatedAt, lastSavedAt) sẽ được tự động lấy theo giờ hiện tại của hệ thống.
     *
     * @param senderEmail  Email người gửi
     * @param toRecipients Danh sách người nhận chính
     * @param ccRecipients Danh sách người nhận Cc
     * @param bccRecipients Danh sách người nhận Bcc
     * @param subject      Tiêu đề email
     * @param body         Nội dung email
     */
    public DraftEmail(String senderEmail, String toRecipients, String ccRecipients,
                      String bccRecipients, String subject, String body) {
        this.senderEmail = senderEmail;
        this.toRecipients = toRecipients;
        this.ccRecipients = ccRecipients;
        this.bccRecipients = bccRecipients;
        this.subject = subject;
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastSavedAt = LocalDateTime.now();
    }

    /**
     * Ghi đè phương thức toString để hỗ trợ việc in log và debug thông tin bản nháp một cách ngắn gọn.
     */
    @Override
    public String toString() {
        return String.format("Draft(id=%d, sender=%s, subject=%s, savedAt=%s)",
                id, senderEmail, subject, lastSavedAt);
    }
}