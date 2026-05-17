package nlu.fit.soft.gr5.precisionMail.service;

import java.util.concurrent.atomic.AtomicInteger;

public final class ApplicationStateService {
    private static final AtomicInteger ACTIVE_EMAIL_SENDS = new AtomicInteger();

    private ApplicationStateService() {
    }

    public static void beginEmailSend() {
        ACTIVE_EMAIL_SENDS.incrementAndGet();
    }

    public static void endEmailSend() {
        ACTIVE_EMAIL_SENDS.updateAndGet(value -> Math.max(0, value - 1));
    }

    public static boolean hasActiveEmailSend() {
        return ACTIVE_EMAIL_SENDS.get() > 0;
    }
}
