package nlu.fit.soft.gr5.precisionMail.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback layout an toàn dùng để sanitize log trước khi ghi ra output.
 *
 * <p>Lớp này mở rộng {@link ch.qos.logback.classic.PatternLayout} và áp dụng
 * {@link LogSanitizer} trên kết quả format cuối cùng.</p>
 */
public class SecurePatternLayout extends PatternLayout {
    /**
     * Format một logging event rồi làm sạch dữ liệu nhạy cảm trước khi trả về.
     *
     * @param event sự kiện log do Logback truyền vào
     * @return dòng log đã được sanitize
     */
    @Override
    public String doLayout(ILoggingEvent event) {
        return LogSanitizer.sanitize(super.doLayout(event));
    }
}
