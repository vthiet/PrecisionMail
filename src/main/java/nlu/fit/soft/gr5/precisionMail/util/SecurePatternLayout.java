package nlu.fit.soft.gr5.precisionMail.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SecurePatternLayout extends PatternLayout {
    @Override
    public String doLayout(ILoggingEvent event) {
        return LogSanitizer.sanitize(super.doLayout(event));
    }
}
