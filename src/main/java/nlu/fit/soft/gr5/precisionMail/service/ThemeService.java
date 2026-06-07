package nlu.fit.soft.gr5.precisionMail.service;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Theme;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public final class ThemeService {
    private static final Theme LIGHT_THEME = new CupertinoLight();
    private static final Theme DARK_THEME = new CupertinoDark();
    private static final BooleanProperty DARK_MODE = new SimpleBooleanProperty(false);

    static {
        DARK_MODE.addListener((observable, oldValue, darkMode) -> applyTheme(darkMode));
    }

    private ThemeService() {
    }

    public static void initialize() {
        applyTheme(DARK_MODE.get());
    }

    public static BooleanProperty darkModeProperty() {
        return DARK_MODE;
    }

    private static void applyTheme(boolean darkMode) {
        Theme theme = darkMode ? DARK_THEME : LIGHT_THEME;
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
    }
}
