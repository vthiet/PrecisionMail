package nlu.fit.soft.gr5.precisionMail.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service contract cho UC-07 - giám sát và xử lý file log hệ thống.
 *
 * <p>Interface này tách phần xử lý file log khỏi JavaFX controller, giúp UI chỉ
 * cần gọi service mà không phụ thuộc trực tiếp vào cách đọc, lọc, export hoặc
 * theo dõi file log.</p>
 */
public interface LogMonitoringService {
    /**
     * Đọc các dòng log mới nhất từ file log hiện tại.
     *
     * @param maxLines số dòng tối đa cần trả về
     * @return danh sách dòng log đã được làm sạch dữ liệu nhạy cảm
     * @throws IOException nếu không thể đọc file log
     */
    List<String> readRecentLines(int maxLines) throws IOException;

    /**
     * Đọc log từ file và lọc theo level/từ khóa.
     *
     * @param level level cần lọc, ví dụ ALL, ERROR, WARN, INFO, DEBUG
     * @param keyword từ khóa cần tìm trong dòng log
     * @param maxLines số dòng tối đa cần trả về
     * @return danh sách dòng log phù hợp điều kiện lọc
     * @throws IOException nếu không thể đọc file log
     */
    List<String> streamAndFilterLogs(String level, String keyword, int maxLines) throws IOException;

    /**
     * Đóng gói file log hiện tại thành file ZIP.
     *
     * @param destination đường dẫn file ZIP đích
     * @return đường dẫn file ZIP đã tạo
     * @throws IOException nếu file log không tồn tại hoặc không thể ghi file ZIP
     */
    Path exportActiveLogs(Path destination) throws IOException;

    /**
     * Theo dõi file log hiện tại và callback khi có dòng log mới.
     *
     * @param newLinesConsumer callback nhận danh sách dòng log mới
     * @return registration dùng để dừng watcher
     * @throws IOException nếu không thể đăng ký watcher trên thư mục log
     */
    LogWatchRegistration watchActiveLog(Consumer<List<String>> newLinesConsumer) throws IOException;

    /**
     * Trả về đường dẫn file log hệ thống đang hoạt động.
     *
     * @return đường dẫn file system.log hiện tại
     */
    Path activeLogFile();

    /**
     * Trả về thư mục chứa file log hệ thống.
     *
     * @return thư mục log của ứng dụng
     */
    Path activeLogDirectory();

    /**
     * Kiểm tra file log hiện tại có vượt ngưỡng an toàn cho UI hay không.
     *
     * @return true nếu file log quá lớn để tải toàn bộ lên giao diện
     * @throws IOException nếu không thể đọc thông tin kích thước file
     */
    boolean isActiveLogOversized() throws IOException;

    /**
     * Handle dùng để dừng tiến trình theo dõi file log.
     */
    interface LogWatchRegistration extends AutoCloseable {
        /**
         * Dừng watcher và giải phóng tài nguyên liên quan.
         */
        @Override
        void close();
    }
}
