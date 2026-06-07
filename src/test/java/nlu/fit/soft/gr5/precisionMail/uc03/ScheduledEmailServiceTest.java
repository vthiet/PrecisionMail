package nlu.fit.soft.gr5.precisionMail.uc03;

import nlu.fit.soft.gr5.precisionMail.service.impl.ScheduledEmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledEmailServiceTest {

    private ScheduledEmailServiceImpl service;

    @BeforeEach
    void setUp() {
        service = ScheduledEmailServiceImpl.getInstance();
        service.resumeQueue();
    }

    @Test
    void testPauseQueue_ShouldSetFlagToTrue() {
        service.pauseQueue();
        assertTrue(service.isQueuePaused(), "Hàng đợi phải ở trạng thái Tạm dừng (true) sau khi gọi pauseQueue");
    }

    @Test
    void testResumeQueue_ShouldSetFlagToFalse() {
        service.pauseQueue();
        service.resumeQueue();

        assertFalse(service.isQueuePaused(), "Hàng đợi phải ở trạng thái Đang chạy (false) sau khi gọi resumeQueue");
    }
}