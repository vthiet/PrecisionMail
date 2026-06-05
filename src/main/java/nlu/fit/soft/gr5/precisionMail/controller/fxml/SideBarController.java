package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.controller.dialog.AddAccountDialogController;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SideBarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SideBarController.class);
    private static final String COMPOSE_VIEW = "center/compose-mail.fxml";
    private static final String HISTORY_VIEW = "center/history-mail.fxml";
    private static final String QUEUE_VIEW = "center/queue-mail.fxml";
    private static final String LOG_VIEW = "center/system-logs.fxml";
    private static final String STATISTICS_VIEW = "center/statistics-view.fxml";
    private static final String COMPOSE_ACTIVE_STYLE = "compose-button-active";
    private static final String NAV_ACTIVE_STYLE = "sidebar-nav-item-active";

    @FXML
    public Button composeButton;
    @FXML
    public Button btnDrafts;
    @FXML
    public Button btnSent;
    @FXML
    public Button btnQueue;
    @FXML
    public Button btnLogs;
    public Button btnStats;

    @FXML
    public void initialize() {
        NavigationService navigationService = NavigationService.getInstance();
        navigationService.addNavigationObserver(this::updateActiveButton);
        updateActiveButton(navigationService.getCurrentView());
    }

    @FXML
    public void handleStatsBtn(ActionEvent actionEvent) {
        NavigationService.getInstance().navigateTo(STATISTICS_VIEW);
    }

    @FXML
    public void onNewMailClick(ActionEvent actionEvent) {
        LOGGER.info("Sidebar requested navigation to compose-mail view.");
        NavigationService.getInstance().navigateTo(COMPOSE_VIEW);
    }

    @FXML
    public void handleSentMailBtn(ActionEvent actionEvent) {
        LOGGER.info("Sidebar requested navigation to history-mail view.");
        NavigationService.getInstance().navigateTo(HISTORY_VIEW);
    }

    @FXML
    public void handleQueueBtn(ActionEvent actionEvent) {
        LOGGER.info("Sidebar requested navigation to queue-mail view.");
        NavigationService.getInstance().navigateTo(QUEUE_VIEW);
    }

    @FXML
    public void handleLogsBtn(ActionEvent actionEvent) {
        LOGGER.info("Sidebar requested navigation to system-log view.");
        NavigationService.getInstance().navigateTo(LOG_VIEW);
    }

    @FXML
    public void handleSettings(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/nlu/fit/soft/gr5/precisionMail/view/dialog/add-account-dialog.fxml")
            );
            Parent root = loader.load();
            AddAccountDialogController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            stage.setTitle("Cấu hình tài khoản");
            stage.setScene(new Scene(root, 520, 430));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            LOGGER.info("Account-configuration dialog closed from sidebar.");
        } catch (IOException e) {
            LOGGER.error("Failed to open account-configuration dialog from sidebar.", e);
        }
    }

    private void updateActiveButton(String currentView) {
        setActive(composeButton, COMPOSE_ACTIVE_STYLE, COMPOSE_VIEW.equals(currentView));
        setActive(btnSent, NAV_ACTIVE_STYLE, HISTORY_VIEW.equals(currentView));
        setActive(btnQueue, NAV_ACTIVE_STYLE, QUEUE_VIEW.equals(currentView));
        setActive(btnLogs, NAV_ACTIVE_STYLE, LOG_VIEW.equals(currentView));
        setActive(btnStats, NAV_ACTIVE_STYLE, STATISTICS_VIEW.equals(currentView));
        setActive(btnDrafts, NAV_ACTIVE_STYLE, false);
    }

    private void setActive(Button button, String activeStyleClass, boolean active) {
        if (button == null) {
            return;
        }
        button.getStyleClass().remove(activeStyleClass);
        if (active) {
            button.getStyleClass().add(activeStyleClass);
        }
    }
}
