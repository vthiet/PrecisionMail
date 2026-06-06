package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.controller.dialog.AddAccountDialogController;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MenuBarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuBarController.class);
    private static final String ADD_ACCOUNT_DIALOG =
            "/nlu/fit/soft/gr5/precisionMail/view/dialog/add-account-dialog.fxml";

    @FXML
    public void onNewMailClick(ActionEvent actionEvent) {
        LOGGER.info("Navigate to compose-mail view requested from menu.");
        NavigationService.getInstance().navigateTo("center/compose-mail.fxml");
    }

    public void handleAddAccount(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ADD_ACCOUNT_DIALOG));
            Parent root = loader.load();
            AddAccountDialogController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.prepareNewAccount();
            stage.setTitle("Thêm tài khoản");
            stage.setScene(new Scene(root, 540, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            LOGGER.info("Add-account dialog closed.");
        } catch (IOException e) {
            LOGGER.error("Failed to open add-account dialog.", e);
        }
    }

    @FXML
    public void handleManageAccounts(ActionEvent actionEvent) {
        LOGGER.info("Navigate to account-management view requested from menu.");
        NavigationService.getInstance().navigateTo("center/accounts-management.fxml");
    }

    @FXML
    public void handleSystemLogs(ActionEvent actionEvent) {
        LOGGER.info("Navigate to system-log view requested from menu.");
        NavigationService.getInstance().navigateTo("center/system-logs.fxml");
    }
}
