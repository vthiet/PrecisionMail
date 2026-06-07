package nlu.fit.soft.gr5.precisionMail.uc03;

import nlu.fit.soft.gr5.precisionMail.service.AccountRateLimiter;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AccountRateLimiterTest {

    @Test
    void testTryAcquire_ShouldConsumeTokensUntilEmpty() {
        AccountRateLimiter limiter = new AccountRateLimiter(3, Duration.ofSeconds(10));
        long accountId = 1L;

        assertTrue(limiter.tryAcquire(accountId), "Lần 1 phải thành công");
        assertTrue(limiter.tryAcquire(accountId), "Lần 2 phải thành công");
        assertTrue(limiter.tryAcquire(accountId), "Lần 3 phải thành công");

        assertFalse(limiter.tryAcquire(accountId), "Lần 4 phải thất bại vì đã hết token");
    }

    @Test
    void testTryAcquire_ShouldIsolateDifferentAccounts() {
        AccountRateLimiter limiter = new AccountRateLimiter(1, Duration.ofSeconds(10));
        long account1 = 101L;
        long account2 = 102L;

        assertTrue(limiter.tryAcquire(account1), "Account 1 lần 1 thành công");
        assertFalse(limiter.tryAcquire(account1), "Account 1 lần 2 thất bại");

        assertTrue(limiter.tryAcquire(account2), "Account 2 phải thành công (không bị ảnh hưởng bởi account 1)");
    }

    @Test
    void testEstimateWait_ShouldReturnCorrectDuration() {
        AccountRateLimiter limiter = new AccountRateLimiter(1, Duration.ofSeconds(5));
        long accountId = 1L;
        assertEquals(Duration.ZERO, limiter.estimateWait(accountId), "Thời gian đợi ban đầu phải là ZERO");
        // Rút cạn token
        limiter.tryAcquire(accountId);
        Duration waitTime = limiter.estimateWait(accountId);
        assertTrue(waitTime.toNanos() > 0, "Thời gian đợi phải lớn hơn 0 khi đã hết token");
        assertTrue(waitTime.toSeconds() <= 5, "Thời gian đợi không được vượt quá chu kỳ nạp (5s)");
    }

    @Test
    void testRefill_ShouldReplenishTokensAfterDelay() throws InterruptedException {
        AccountRateLimiter limiter = new AccountRateLimiter(5, Duration.ofMillis(100));
        long accountId = 1L;
        for (int i = 0; i < 5; i++) {
            limiter.tryAcquire(accountId);
        }
        assertFalse(limiter.tryAcquire(accountId), "Đã hết token, phải trả về false");
        // Cho Thread ngủ 150 mili-giây (Đủ để qua 1 chu kỳ 100ms)
        Thread.sleep(150);

        assertTrue(limiter.tryAcquire(accountId), "Phải lấy được token sau khi hệ thống refill");
    }

    @Test
    void testConstructor_ShouldThrowExceptionOnInvalidInputs() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            new AccountRateLimiter(0, Duration.ofSeconds(1));
        });
        assertEquals("capacity must be > 0", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            new AccountRateLimiter(10, Duration.ZERO);
        });
        assertEquals("refillPeriod must be > 0", ex2.getMessage());
    }
}