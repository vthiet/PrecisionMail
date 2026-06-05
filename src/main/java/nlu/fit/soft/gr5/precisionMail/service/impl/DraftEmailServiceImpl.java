package nlu.fit.soft.gr5.precisionMail.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nlu.fit.soft.gr5.precisionMail.dao.DraftEmailDao;
import nlu.fit.soft.gr5.precisionMail.dao.impl.DraftEmailDaoImpl;
import nlu.fit.soft.gr5.precisionMail.model.DraftEmail;
import nlu.fit.soft.gr5.precisionMail.service.DraftEmailService;

/**
 * Lớp triển khai (Implementation) của interface DraftEmailService.
 * Chịu trách nhiệm xử lý các logic nghiệp vụ như lưu, tải và quản lý các thư điện tử nháp.
 */
public class DraftEmailServiceImpl implements DraftEmailService {
    // Đối tượng Logger dùng để ghi nhận log (lịch sử hoạt động, lỗi) của lớp này
    private static final Logger LOGGER = LoggerFactory.getLogger(DraftEmailServiceImpl.class);

    // Đối tượng Data Access Object (DAO) để thao tác trực tiếp với cơ sở dữ liệu hoặc file lưu trữ
    private final DraftEmailDao dao = new DraftEmailDaoImpl();

    /**
     * Thực hiện lưu mới hoặc cập nhật một bản nháp email.
     * Kiểm tra tính hợp lệ của email người gửi trước khi tiến hành lưu xuống DB.
     */
    @Override
    public DraftEmail saveDraft(DraftEmail draft) throws IOException {
        // Kiểm tra validation: Nếu không có email người gửi hoặc chỉ chứa khoảng trắng thì báo lỗi
        if (draft.senderEmail == null || draft.senderEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email người gửi không được để trống");
        }

        try {
            // Gọi tầng DAO để thực hiện lưu trữ thông tin
            DraftEmail saved = dao.save(draft);
            LOGGER.debug("Đã lưu thư nháp thành công: id={}, sender={}", saved.id, saved.senderEmail);
            return saved;
        } catch (IOException e) {
            // Ghi nhận log lỗi kèm theo stack trace để phục vụ cho việc debug
            LOGGER.error("Gặp lỗi khi đang lưu thư nháp.", e);
            throw e; // Ném tiếp ngoại lệ lên tầng phía trên xử lý (Controller/UI)
        }
    }

    /**
     * Tải thông tin một bản nháp email cụ thể dựa trên mã ID.
     */
    @Override
    public Optional<DraftEmail> loadDraft(Long id) throws IOException {
        try {
            return dao.findById(id);
        } catch (IOException e) {
            LOGGER.error("Gặp lỗi khi tải thư nháp theo ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Lấy danh sách toàn bộ các thư nháp thuộc sở hữu của một email người gửi cụ thể.
     */
    @Override
    public List<DraftEmail> loadDraftsForSender(String senderEmail) throws IOException {
        try {
            // LƯU Ý: Tên hàm ở tầng DAO đang bị sai chính tả nhẹ (findBySneder -> nên sửa thành findBySender)
            List<DraftEmail> drafts = dao.findBySneder(senderEmail);
            LOGGER.debug("Đã tải thành công {} thư nháp của người gửi: {}", drafts.size(), senderEmail);
            return drafts;
        } catch (IOException e) {
            LOGGER.error("Gặp lỗi khi tải danh sách thư nháp của người gửi: {}", senderEmail, e);
            throw e;
        }
    }

    /**
     * Lấy toàn bộ danh sách thư nháp có trong hệ thống (tối đa 1000 bản ghi theo đặc tả interface).
     */
    @Override
    public List<DraftEmail> loadAllDrafts() throws IOException {
        try {
            return dao.findAll();
        } catch (IOException e) {
            LOGGER.error("Gặp lỗi khi tải toàn bộ danh sách thư nháp hệ thống.", e);
            throw e;
        }
    }

    /**
     * Xóa một thư nháp cụ thể dựa vào mã ID được cung cấp.
     */
    @Override
    public void deleteDraft(Long id) throws IOException {
        try {
            dao.deleteById(id);
            LOGGER.debug("Đã xóa thư nháp thành công: id={}", id);
        } catch (IOException e) {
            LOGGER.error("Gặp lỗi khi xóa thư nháp có ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Xóa sạch toàn bộ các thư nháp liên quan đến email của một người gửi cụ thể.
     */
    @Override
    public void deleteAllDraftsForSender(String senderEmail) throws IOException {
        try {
            dao.deleteAllForSender(senderEmail);
            LOGGER.info("Đã xóa toàn bộ thư nháp của người gửi: {}", senderEmail);
        } catch (IOException e) {
            LOGGER.error("Gặp lỗi khi xóa toàn bộ thư nháp của người gửi: {}", senderEmail, e);
            throw e;
        }
    }

    /**
     * Hệ thống tự động dọn dẹp và xóa bỏ các thư nháp cũ đã tồn tại vượt quá số ngày quy định.
     */
    @Override
    public void cleanupOldDrafts(int days) throws IOException {
        try {
            dao.deleteOlderThan(days);
            LOGGER.info("Hoàn tất dọn dẹp các thư nháp cũ hơn {} ngày.", days);
        } catch (IOException e) {
            LOGGER.error("Gặp lỗi trong quá trình tự động dọn dẹp thư nháp cũ.", e);
            throw e;
        }
    }
}