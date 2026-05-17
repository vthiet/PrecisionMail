package nlu.fit.soft.gr5.precisionMail.util;

import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertUtil.class);

    public static void showError(String title, String message) {
        LOGGER.warn("Showing error alert. title={}.", title);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        LOGGER.info("Showing info alert. title={}.", title);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
