package nlu.fit.soft.gr5.precisionMail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class NavigationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationService.class);
    private static NavigationService instance;

    private Consumer<String> onNavigate;

    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    public void setNavigationListener(Consumer<String> listener) {
        this.onNavigate = listener;
    }

    public void navigateTo(String fxmlFileName) {
        LOGGER.info("Navigation requested to {}.", fxmlFileName);

        if (fxmlFileName != null && onNavigate != null) {
            onNavigate.accept(fxmlFileName);
        }
    }
}
