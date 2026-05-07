package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;

public class MenuBarController {

    @FXML
    public void onNewMailClick(ActionEvent actionEvent) {
        NavigationService.getInstance().navigateTo("center/compose-mail.fxml");
    }
}
