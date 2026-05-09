package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;

public class SideBarController {

    public Button btnSent;

    @FXML
    public void onNewMailClick(ActionEvent actionEvent) {
        NavigationService.getInstance().navigateTo("center/compose-mail.fxml");
    }

    @FXML
    public void handleSentMailBtn(ActionEvent actionEvent) {
        NavigationService.getInstance().navigateTo("center/history-mail.fxml");
    }
}
