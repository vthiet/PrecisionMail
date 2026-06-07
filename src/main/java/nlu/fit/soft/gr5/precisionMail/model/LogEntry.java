package nlu.fit.soft.gr5.precisionMail.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model biểu diễn một dòng log đã được parse để hiển thị trên bảng UC-07.
 *
 * @param timestamp thời điểm ghi log
 * @param level cấp độ log, ví dụ ERROR, WARN, INFO, DEBUG
 * @param thread tên thread ghi log
 * @param source class hoặc logger source
 * @param message nội dung log chính
 * @param rawLine dòng log gốc sau khi đã sanitize
 */
public record LogEntry(
        String timestamp,
        String level,
        String thread,
        String source,
        String message,
        String rawLine
) {
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^\\[(?<timestamp>[^]]+)] \\[(?<level>[^]]+)] \\[(?<thread>[^]]+)] \\[(?<source>[^]]+)] - (?<message>.*)$"
    );

    /**
     * Parse một dòng log thô theo pattern chuẩn của logback.
     *
     * <p>Nếu dòng log không đúng format, phương thức vẫn trả về {@code LogEntry}
     * với phần message/rawLine là nội dung gốc để UI không bị mất dữ liệu.</p>
     *
     * @param line dòng log cần parse
     * @return log entry dùng cho bảng hiển thị
     */
    public static LogEntry parse(String line) {
        String safeLine = line == null ? "" : line;
        Matcher matcher = LOG_PATTERN.matcher(safeLine);
        if (!matcher.matches()) {
            return new LogEntry("", "", "", "", safeLine, safeLine);
        }
        return new LogEntry(
                matcher.group("timestamp"),
                matcher.group("level").trim(),
                matcher.group("thread"),
                matcher.group("source"),
                matcher.group("message"),
                safeLine
        );
    }
}
