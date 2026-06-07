package nlu.fit.soft.gr5.precisionMail.infrastructure.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class AppExecutors {
    // NFR-06-01: all UC-06 file I/O, grouping, filtering, watching, and ZIP export run off the JavaFX thread.
    private static final ExecutorService IO_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(2, r -> {
                Thread thread = new Thread(r, "precisionmail-scheduler");
                thread.setDaemon(true);
                return thread;
            });

    private AppExecutors() {
    }

    public static ExecutorService io() {
        return IO_EXECUTOR;
    }

    public static ScheduledExecutorService scheduler() {
        return SCHEDULER;
    }

    public static void shutdown() {
        IO_EXECUTOR.shutdown();
        SCHEDULER.shutdown();
    }
}
