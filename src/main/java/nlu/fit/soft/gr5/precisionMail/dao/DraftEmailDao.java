package nlu.fit.soft.gr5.precisionMail.dao;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import nlu.fit.soft.gr5.precisionMail.model.DraftEmail;

/**
 * Interface DAO (Data Access Object) định nghĩa các thao tác CRUD và truy vấn dữ liệu trực tiếp
 * đối với thực thể DraftEmail trong cơ sở dữ liệu hoặc hệ thống lưu trữ.
 */
public interface DraftEmailDao {

    /**
     * Lưu mới một bản nháp hoặc cập nhật thông tin nếu bản nháp đã tồn tại.
     *
     * @param draft Đối tượng DraftEmail chứa thông tin cần lưu trữ
     * @return Đối tượng DraftEmail sau khi lưu thành công (đã kèm theo ID tự sinh nếu là tạo mới)
     * @throws IOException Nếu xảy ra lỗi kết nối hoặc thao tác với cơ sở dữ liệu/hệ thống lưu trữ file
     */
    DraftEmail save(DraftEmail draft) throws IOException;

    /**
     * Tìm kiếm một bản nháp email cụ thể dựa trên mã ID.
     *
     * @param id Mã định danh duy nhất của bản nháp cần tìm
     * @return Một Optional chứa đối tượng DraftEmail nếu tìm thấy, hoặc Optional.empty() nếu không tồn tại
     * @throws IOException Nếu xảy ra lỗi trong quá trình truy vấn dữ liệu từ DB
     */
    Optional<DraftEmail> findById(Long id) throws IOException;

    /**
     * Tìm kiếm danh sách các bản nháp thuộc về một email người gửi cụ thể.
     * * LƯU Ý: Tên hàm đang bị sai chính tả ở chữ "Sneder" (Đúng ra phải là Sender).
     * Bạn có thể cân nhắc refactor đổi tên thành `findBySender` để code chuẩn hơn.
     *
     * @param senderEmail Địa chỉ email của người gửi cần tìm kiếm thư nháp
     * @return Danh sách (List) các bản nháp tìm được, trả về danh sách rỗng nếu không có dữ liệu
     * @throws IOException Nếu xảy ra lỗi truy xuất dữ liệu từ DB
     */
    List<DraftEmail> findBySneder(String senderEmail) throws IOException;

    /**
     * Lấy toàn bộ danh sách bản nháp email hiện có trong hệ thống (Giới hạn tối đa 1000 bản ghi).
     *
     * @return Danh sách chứa tối đa 1000 bản nháp trên toàn hệ thống
     * @throws IOException Nếu xảy ra lỗi trong quá trình đọc dữ liệu hàng loạt
     */
    List<DraftEmail> findAll() throws IOException;

    /**
     * Xóa một bản nháp khỏi hệ thống dựa vào mã ID.
     *
     * @param id Mã định danh của bản nháp cần xóa
     * @throws IOException Nếu xảy ra lỗi trong quá trình xóa dữ liệu khỏi hệ thống lưu trữ
     */
    void deleteById(Long id) throws IOException;

    /**
     * Xóa tất cả các bản nháp đang có của một người gửi cụ thể.
     *
     * @param senderEmail Địa chỉ email của người sở hữu các bản nháp cần xóa hoàn toàn
     * @throws IOException Nếu xảy ra lỗi trong quá trình thực thi lệnh xóa hàng loạt
     */
    void deleteAllForSender(String senderEmail) throws IOException;

    /**
     * Xóa bỏ các bản nháp cũ dựa trên số ngày lưu trữ quy định.
     * Các bản nháp có thời gian cập nhật/tạo cũ hơn số ngày này sẽ bị loại bỏ khỏi hệ thống.
     *
     * @param days Số ngày giới hạn để xác định các bản nháp đã quá hạn cần dọn dẹp
     * @throws IOException Nếu xảy ra lỗi trong quá trình quét và xóa dữ liệu cũ
     */
    void deleteOlderThan(int days) throws IOException;
}