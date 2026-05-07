package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ComposeMailController {

    @FXML public Label ccLabel;
    @FXML public TextField ccField;
    @FXML public Label bccLabel;
    @FXML public TextField bccField;
    @FXML public Button ccBtn;
    @FXML public Button bccBtn;

    @FXML
    public void initialize(){
        ccBtn.managedProperty().bind(ccBtn.visibleProperty());
        bccBtn.managedProperty().bind(bccBtn.visibleProperty());

        ccLabel.managedProperty().bind(ccLabel.visibleProperty());
        bccLabel.managedProperty().bind(bccLabel.visibleProperty());

        ccField.managedProperty().bind(ccField.visibleProperty());
        bccField.managedProperty().bind(bccField.visibleProperty());

        ccLabel.visibleProperty().bind(ccBtn.visibleProperty().not());
        ccField.visibleProperty().bind(ccBtn.visibleProperty().not());

        bccLabel.visibleProperty().bind(bccBtn.visibleProperty().not());
        bccField.visibleProperty().bind(bccBtn.visibleProperty().not());
    }

    @FXML
    public void toggleCc(ActionEvent actionEvent) {
        ccBtn.setVisible(false);
    }

    @FXML
    public void toggleBcc(ActionEvent actionEvent) {
        bccBtn.setVisible(false);
    }
}