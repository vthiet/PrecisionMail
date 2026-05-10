package nlu.fit.soft.gr5.precisionMail.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AccountRefreshService {
    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    private AccountRefreshService() {
    }

    public static void subscribe(Runnable listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void publishAccountsChanged() {
        for (Runnable listener : LISTENERS) {
            listener.run();
        }
    }
}
