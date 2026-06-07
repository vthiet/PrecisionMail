package nlu.fit.soft.gr5.precisionMail.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SecurePatternLayout extends PatternLayout {
    @Override
    public String doLayout(ILoggingEvent event) {
        // BR-06-01: prevent sensitive values from being persisted before UI-side sanitization.
        return LogSanitizer.sanitize(super.doLayout(event));
    }
}
