package nlu.fit.soft.gr5.precisionMail.model;

/**
 * Trạng thái tiến trình kiểm tra kết nối SMTP/IMAP trong UC-01.
 *
 * <p>Commit UC-01 #18 - Anh Han: giúp UI báo rõ đang kiểm tra SMTP,
 * SMTP đã xong, đang kiểm tra IMAP và IMAP đã xong.</p>
 *
 * @author Anh Han
 */
public enum ConnectionTestProgress {
    SMTP_TESTING,
    SMTP_SUCCEEDED,
    IMAP_TESTING,
    IMAP_SUCCEEDED
}
