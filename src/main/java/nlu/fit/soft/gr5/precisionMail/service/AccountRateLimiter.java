package nlu.fit.soft.gr5.precisionMail.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AccountRateLimiter {

    private final long capacity;
    private final long refillPeriodNanos;

    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();

    public AccountRateLimiter(long capacity, Duration refillPeriod) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (refillPeriod == null || refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("refillPeriod must be > 0");
        }
        this.capacity = capacity;
        this.refillPeriodNanos = refillPeriod.toNanos();
    }

    public boolean tryAcquire(long accountId) {
        long now = System.nanoTime();
        synchronized (buckets) {
            Bucket b = buckets.computeIfAbsent(accountId, __ -> new Bucket(capacity, now));

            refill(b, now);
            if (b.tokens >= 1) {
                b.tokens--;
                return true;
            }
            return false;
        }
    }

    public Duration estimateWait(long accountId) {
        long now = System.nanoTime();
        synchronized (buckets) {
            Bucket b = buckets.computeIfAbsent(accountId, __ -> new Bucket(capacity, now));

            refill(b, now);
            if (b.tokens >= 1) return Duration.ZERO;

            // Tính toán thời gian chính xác cần đợi cho đến lượt refill tiếp theo
            long nanosSinceLastRefill = now - b.lastRefillNanos;
            long nanosPerToken = refillPeriodNanos / capacity;
            long nanosToWait = nanosPerToken - nanosSinceLastRefill;

            return Duration.ofNanos(Math.max(0, nanosToWait));
        }
    }

    private void refill(Bucket b, long now) {
        long elapsed = now - b.lastRefillNanos;
        if (elapsed <= 0) return;

        long nanosPerToken = refillPeriodNanos / capacity;
        long tokensToAdd = elapsed / nanosPerToken;

        if (tokensToAdd > 0) {
            b.tokens = Math.min(capacity, b.tokens + tokensToAdd);
            // Chỉ dịch chuyển mốc thời gian tương ứng với số token đã nạp để không làm mất phần dư (remainder nanos)
            b.lastRefillNanos += tokensToAdd * nanosPerToken;
        }
    }

    private static final class Bucket {
        long tokens;
        long lastRefillNanos;

        Bucket(long tokens, long lastRefillNanos) {
            this.tokens = tokens;
            this.lastRefillNanos = lastRefillNanos;
        }
    }
}