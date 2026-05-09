package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.controller.dialog.AddAccountDialogController;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;

import java.io.IOException;

public class MenuBarController {

    @FXML
    public void onNewMailClick(ActionEvent actionEvent) {
        NavigationService.getInstance().navigateTo("center/compose-mail.fxml");
    }

    public void handleAddAccount(ActionEvent actionEvent) {
        // Show dialog
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/nlu/fit/soft/gr5/precisionMail/view/dialog/add-account-dialog.fxml"
                    )
            );
            Parent root = loader.load();
            AddAccountDialogController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            stage.setTitle("Add Account");
            stage.setScene(new Scene(root, 250, 180));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
