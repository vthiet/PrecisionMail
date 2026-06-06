package nlu.fit.soft.gr5.precisionMail.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import nlu.fit.soft.gr5.precisionMail.model.DraftEmail;

/**
 * Service Interface định nghĩa các thao tác xử lý nghiệp vụ (Business Logic) cho bản nháp email (DraftEmail).
 * Hỗ trợ tính năng tự động lưu bản nháp (auto-saving) định kỳ mỗi 30 giây.
 */
public interface DraftEmailService {

    /**
     * Lưu mới hoặc cập nhật một bản nháp email.
     * Nếu bản nháp đã tồn tại (có ID), hệ thống sẽ cập nhật nội dung mới.
     * Nếu chưa có ID, hệ thống sẽ tạo mới một bản ghi.
     *
     * @param draft Đối tượng DraftEmail chứa thông tin cần lưu
     * @return Đối tượng DraftEmail sau khi đã được lưu thành công (bao gồm ID nếu tạo mới)
     * @throws IOException Nếu xảy ra lỗi trong quá trình ghi dữ liệu (ví dụ: lỗi file đính kèm hoặc database)
     */
    DraftEmail saveDraft(DraftEmail draft) throws IOException;

    /**
     * Tải (Tìm kiếm) một bản nháp email cụ thể dựa vào ID.
     *
     * @param id Mã định danh của bản nháp cần tìm
     * @return Một Optional chứa DraftEmail nếu tìm thấy, hoặc Optional.empty() nếu không tồn tại
     * @throws IOException Nếu xảy ra lỗi khi đọc dữ liệu từ nguồn lưu trữ
     */
    Optional<DraftEmail> loadDraft(Long id) throws IOException;

    /**
     * Tải tất cả các bản nháp thuộc về một người gửi cụ thể.
     * Thường dùng để hiển thị danh sách trong thư mục "Thư nháp" (Drafts) của tài khoản đó.
     *
     * @param senderEmail Địa chỉ email của người gửi cần lấy danh sách bản nháp
     * @return Danh sách (List) các bản nháp của người gửi, trả về danh sách rỗng nếu không có
     * @throws IOException Nếu xảy ra lỗi khi truy vấn dữ liệu
     */
    List<DraftEmail> loadDraftsForSender(String senderEmail) throws IOException;

    /**
     * Tải toàn bộ bản nháp hiện có trong hệ thống (Giới hạn tối đa 1000 bản ghi).
     * Thường dùng cho mục đích quản trị hệ thống hoặc kiểm tra dữ liệu.
     *
     * @return Danh sách chứa tối đa 1000 bản nháp trên toàn hệ thống
     * @throws IOException Nếu xảy ra lỗi trong quá trình truy xuất dữ liệu hàng loạt
     */
    List<DraftEmail> loadAllDrafts() throws IOException;

    /**
     * Xóa một bản nháp email cụ thể dựa vào ID.
     * Thường được gọi khi người dùng bấm xóa nháp hoặc khi email nháp này đã được gửi đi thành công.
     *
     * @param id Mã định danh của bản nháp cần xóa
     * @throws IOException Nếu xảy ra lỗi trong quá trình xóa dữ liệu hoặc tệp tin đính kèm liên quan
     */
    void deleteDraft(Long id) throws IOException;

    /**
     * Xóa toàn bộ các bản nháp của một người gửi cụ thể.
     * Thường dùng khi người dùng chọn tính năng "Xóa sạch thư mục nháp".
     *
     * @param senderEmail Địa chỉ email của người sở hữu các bản nháp cần xóa
     * @throws IOException Nếu xảy ra lỗi khi thực hiện xóa hàng loạt
     */
    void deleteAllDraftsForSender(String senderEmail) throws IOException;

    /**
     * Tự động dọn dẹp các bản nháp cũ đã quá hạn (Dựa trên số ngày quy định).
     * Thường được cấu hình chạy ngầm (Scheduled Task) để tối ưu dung lượng bộ nhớ.
     *
     * @param days Số ngày giới hạn, các bản nháp cũ hơn số ngày này sẽ bị xóa bỏ
     * @throws IOException Nếu xảy ra lỗi trong quá trình quét và xóa dữ liệu cũ
     */
    void cleanupOldDrafts(int days) throws IOException;
}