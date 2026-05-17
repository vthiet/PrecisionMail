package nlu.fit.soft.gr5.precisionMail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class NavigationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationService.class);
    private static NavigationService instance;

    private Consumer<String> onNavigate;
    private final List<Consumer<String>> navigationObservers = new CopyOnWriteArrayList<>();
    private String currentView;

    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    public void setNavigationListener(Consumer<String> listener) {
        this.onNavigate = listener;
    }

    public void addNavigationObserver(Consumer<String> observer) {
        if (observer != null) {
            navigationObservers.add(observer);
        }
    }

    public String getCurrentView() {
        return currentView;
    }

    public void navigateTo(String fxmlFileName) {
        LOGGER.info("Navigation requested to {}.", fxmlFileName);

        if (fxmlFileName != null) {
            currentView = fxmlFileName;
            if (onNavigate != null) {
                onNavigate.accept(fxmlFileName);
            }
            navigationObservers.forEach(observer -> observer.accept(fxmlFileName));
        }
    }
}
