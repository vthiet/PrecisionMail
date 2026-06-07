package nlu.fit.soft.gr5.precisionMail.uc01;

import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Kiểm tra cấu trúc JUnit ban đầu cho UC-01.
 *
 * <p>Commit UC-01 #2 - Anh Han: xác nhận test có thể load class production
 * và chạy trong module Maven hiện tại.</p>
 *
 * @author Anh Han
 */
class Uc01TestInfrastructureTest {

    @Test
    void junitCanLoadUc01ProductionClasses() {
        MailServerConfig config = new MailServerConfig();

        assertEquals("smtp.gmail.com", config.getSmtpHost());
        assertEquals(587, config.getSmtpPort());
        assertEquals("imap.gmail.com", config.getImapHost());
        assertEquals(993, config.getImapPort());
        assertEquals(SecurityMode.TLS, config.getSmtpSecurityMode());
        assertEquals(SecurityMode.SSL, config.getImapSecurityMode());
    }
}
