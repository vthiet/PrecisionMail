package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainScreenController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainScreenController.class);

    @FXML
    public BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        NavigationService.getInstance().setNavigationListener(this::changeCenterView);
    }

    private void changeCenterView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/nlu/fit/soft/gr5/precisionMail/view/include/" + fxmlFileName)
            );
            Parent newView = loader.load();
            mainBorderPane.setCenter(newView);
            LOGGER.info("Center view changed to {}.", fxmlFileName);
        } catch (IOException e) {
            LOGGER.error("Failed to change center view to {}.", fxmlFileName, e);
        }
    }
}
