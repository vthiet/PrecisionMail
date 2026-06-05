package nlu.fit.soft.gr5.precisionMail.dao.impl;

import nlu.fit.soft.gr5.precisionMail.dao.DraftEmailDao;
import nlu.fit.soft.gr5.precisionMail.model.DraftEmail;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lớp triển khai (Implementation) của DraftEmailDao.
 * Quản lý việc lưu trữ, truy vấn và xóa các bản nháp email trong cơ sở dữ liệu SQLite.
 */
public class DraftEmailDaoImpl implements DraftEmailDao {
    // Đối tượng ghi log cho lớp này
    private static final Logger LOGGER = LoggerFactory.getLogger(DraftEmailDaoImpl.class);

    // Tên bảng lưu trữ dữ liệu bản nháp email trong SQLite
    private static final String TABLE_NAME = "draft_emails";

    /**
     * Lưu một bản nháp email.
     * Tự động điều hướng: Nếu đã có ID thì cập nhật (Update), nếu chưa có thì thêm mới (Insert).
     */
    @Override
    public DraftEmail save(DraftEmail draft) throws IOException {
        // Kiểm tra xem bản nháp đã tồn tại trong DB chưa (nếu id hợp lệ và lớn hơn 0)
        if (draft.id != null && draft.id > 0) {
            return update(draft);
        }

        // Câu lệnh SQL Insert dữ liệu mới
        String sql = "INSERT INTO " + TABLE_NAME +
                " (sender_email, to_recipients, cc_recipients, bcc_recipients, subject, body, attachment_paths, last_saved_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Sử dụng try-with-resources để tự động đóng Connection và PreparedStatement sau khi dùng xong
        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Truyền các giá trị tham số vào câu lệnh SQL
            pstmt.setString(1, draft.senderEmail);
            pstmt.setString(2, draft.toRecipients);
            pstmt.setString(3, draft.ccRecipients);
            pstmt.setString(4, draft.bccRecipients);
            pstmt.setString(5, draft.subject);
            pstmt.setString(6, draft.body);
            pstmt.setString(7, draft.attachmentPaths);
            // Thiết lập các mốc thời gian hiện tại của hệ thống
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            // Nếu thêm mới thành công, tiến hành lấy ID tự động tăng (Generated Key) từ SQLite cấp phát
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        draft.id = rs.getLong(1); // Gán ID mới sinh vào đối tượng
                        draft.lastSavedAt = LocalDateTime.now();
                        LOGGER.debug("Đã thêm mới thư nháp thành công. id={}, sender={}", draft.id, draft.senderEmail);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi thêm mới thư nháp vào cơ sở dữ liệu.", e);
            throw new IOException("Lỗi lưu thư nháp: " + e.getMessage(), e);
        }

        return draft;
    }

    /**
     * Hàm bổ trợ (Private) để cập nhật dữ liệu của bản nháp email đã tồn tại dựa vào ID.
     */
    private DraftEmail update(DraftEmail draft) throws IOException {
        // Câu lệnh SQL Update dữ liệu dựa trên khóa chính id
        String sql = "UPDATE " + TABLE_NAME +
                " SET to_recipients = ?, cc_recipients = ?, bcc_recipients = ?, subject = ?, body = ?, " +
                "attachment_paths = ?, last_saved_at = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, draft.toRecipients);
            pstmt.setString(2, draft.ccRecipients);
            pstmt.setString(3, draft.bccRecipients);
            pstmt.setString(4, draft.subject);
            pstmt.setString(5, draft.body);
            pstmt.setString(6, draft.attachmentPaths);
            // Cập nhật lại thời gian lưu cuối và thời gian chỉnh sửa gần nhất
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(9, draft.id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                draft.lastSavedAt = LocalDateTime.now();
                LOGGER.debug("Đã cập nhật thư nháp thành công. id={}", draft.id);
            }
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi cập nhật thư nháp trong cơ sở dữ liệu.", e);
            throw new IOException("Lỗi cập nhật thư nháp: " + e.getMessage(), e);
        }

        return draft;
    }

    /**
     * Tìm kiếm bản nháp email theo khóa chính ID.
     */
    @Override
    public Optional<DraftEmail> findById(Long id) throws IOException {
        String sql = "SELECT id, sender_email, to_recipients, cc_recipients, bcc_recipients, subject, body, " +
                "attachment_paths, last_saved_at, created_at, updated_at FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ dữ liệu dòng hiện tại thành đối tượng Java và bọc trong Optional
                    return Optional.of(mapResultSetToDraft(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi tìm kiếm thư nháp theo ID.", e);
            throw new IOException("Lỗi truy vấn thư nháp: " + e.getMessage(), e);
        }

        return Optional.empty(); // Trả về rỗng nếu không tìm thấy kết quả
    }

    /**
     * Tìm danh sách thư nháp theo email người gửi (Sắp xếp mới nhất lên đầu, giới hạn 100 bản ghi).
     */
    @Override
    public List<DraftEmail> findBySneder(String senderEmail) throws IOException {
        List<DraftEmail> drafts = new ArrayList<>();
        String sql = "SELECT id, sender_email, to_recipients, cc_recipients, bcc_recipients, subject, body, " +
                "attachment_paths, last_saved_at, created_at, updated_at FROM " + TABLE_NAME +
                " WHERE sender_email = ? ORDER BY last_saved_at DESC LIMIT 100";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, senderEmail);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Duyệt từng hàng kết quả và thêm vào danh sách
                    drafts.add(mapResultSetToDraft(rs));
                }
            }
            LOGGER.debug("Đã tìm thấy {} thư nháp của người gửi: {}", drafts.size(), senderEmail);
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi tìm kiếm danh sách thư nháp theo người gửi.", e);
            throw new IOException("Lỗi truy vấn danh sách thư nháp: " + e.getMessage(), e);
        }

        return drafts;
    }

    /**
     * Lấy toàn bộ bản nháp có trên hệ thống (Sắp xếp mới nhất lên đầu, giới hạn tối đa 1000 bản ghi).
     */
    @Override
    public List<DraftEmail> findAll() throws IOException {
        List<DraftEmail> drafts = new ArrayList<>();
        String sql = "SELECT id, sender_email, to_recipients, cc_recipients, bcc_recipients, subject, body, " +
                "attachment_paths, last_saved_at, created_at, updated_at FROM " + TABLE_NAME +
                " ORDER BY last_saved_at DESC LIMIT 1000";

        try (Connection conn = DbUtil.getConnect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                drafts.add(mapResultSetToDraft(rs));
            }
            LOGGER.info("Đã lấy thành công {} thư nháp từ cơ sở dữ liệu.", drafts.size());
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi lấy toàn bộ danh sách thư nháp hệ thống.", e);
            throw new IOException("Lỗi lấy toàn bộ thư nháp: " + e.getMessage(), e);
        }

        return drafts;
    }

    /**
     * Xóa một bản nháp cụ thể khỏi cơ sở dữ liệu bằng ID.
     */
    @Override
    public void deleteById(Long id) throws IOException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.debug("Đã xóa thư nháp thành công. id={}", id);
            }
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi xóa thư nháp theo ID.", e);
            throw new IOException("Lỗi xóa thư nháp: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa tất cả các bản nháp thuộc về một email người gửi cụ thể.
     */
    @Override
    public void deleteAllForSender(String senderEmail) throws IOException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE sender_email = ?";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, senderEmail);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Đã xóa thành công {} thư nháp của người gửi: {}", affectedRows, senderEmail);
            }
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi xóa tất cả thư nháp của người gửi.", e);
            throw new IOException("Lỗi xóa tất cả thư nháp của người dùng: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa các thư nháp cũ dựa trên số ngày chỉ định.
     * Sử dụng hàm xử lý chuỗi thời gian native của SQLite: `datetime(last_saved_at)`.
     */
    @Override
    public void deleteOlderThan(int days) throws IOException {
        // SQL SQLite: xóa các dòng có last_saved_at nhỏ hơn mốc thời gian hiện tại trừ đi X ngày
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE datetime(last_saved_at) < datetime('now', '-' || ? || ' days')";

        try (Connection conn = DbUtil.getConnect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, days);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("Đã dọn dẹp và xóa {} thư nháp cũ hơn {} ngày.", affectedRows, days);
            }
        } catch (SQLException e) {
            LOGGER.error("Gặp lỗi khi chạy tính năng tự động dọn dẹp thư nháp cũ.", e);
            throw new IOException("Lỗi dọn dẹp hệ thống thư nháp cũ: " + e.getMessage(), e);
        }
    }

    /**
     * Hàm tiện ích (Private) chuyển đổi một dòng bản ghi kết quả (ResultSet) từ SQL
     * sang đối tượng thực thể DraftEmail trong Java.
     */
    private DraftEmail mapResultSetToDraft(ResultSet rs) throws SQLException {
        DraftEmail draft = new DraftEmail();
        draft.id = rs.getLong("id");
        draft.senderEmail = rs.getString("sender_email");
        draft.toRecipients = rs.getString("to_recipients");
        draft.ccRecipients = rs.getString("cc_recipients");
        draft.bccRecipients = rs.getString("bcc_recipients");
        draft.subject = rs.getString("subject");
        draft.body = rs.getString("body");
        draft.attachmentPaths = rs.getString("attachment_paths");

        // Chuyển đổi kiểu dữ liệu từ Timestamp của JDBC sang LocalDateTime của Java 8+
        draft.lastSavedAt = rs.getTimestamp("last_saved_at").toLocalDateTime();
        draft.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        draft.updatedAt = rs.getTimestamp("updated_at").toLocalDateTime();
        return draft;
    }
}