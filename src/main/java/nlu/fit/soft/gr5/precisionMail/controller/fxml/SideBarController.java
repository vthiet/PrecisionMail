package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SideBarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SideBarController.class);

    public Button btnSent;

    @FXML
    public void onNewMailClick(ActionEvent actionEvent) {
        LOGGER.info("Sidebar requested navigation to compose-mail view.");
        NavigationService.getInstance().navigateTo("center/compose-mail.fxml");
    }

    @FXML
    public void handleSentMailBtn(ActionEvent actionEvent) {
        LOGGER.info("Sidebar requested navigation to history-mail view.");
        NavigationService.getInstance().navigateTo("center/history-mail.fxml");
    }
}
