package nlu.fit.soft.gr5.precisionMail.controller.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;
import nlu.fit.soft.gr5.precisionMail.service.AccountRefreshService;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddAccountDialogController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddAccountDialogController.class);

    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;

    private Stage stage;
    private final AccountService accountService = new AccountServiceImpl();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleCancel(ActionEvent actionEvent) {
        stage.close();
    }

    public void handleSave(ActionEvent actionEvent) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().replace(" ", "").trim();

        LOGGER.info("Add-account submitted for username={}.", LogHelper.maskEmail(username));

        if (username.isBlank() || password.isBlank()) {
            LOGGER.warn("Add-account validation failed because username/password is blank.");
            AlertUtil.showError("Validation Error", "Email and Password cannot be empty.");
            return;
        }

        if (!isValidEmail(username)) {
            LOGGER.warn(
                    "Add-account validation failed because email format is invalid. username={}.",
                    LogHelper.maskEmail(username)
            );
            AlertUtil.showError("Validation Error", "Email invalid.");
            return;
        }

        if (password.length() < 16) {
            LOGGER.warn("Add-account validation failed because app password length is too short.");
            AlertUtil.showError("Validation Error", "Google App Password must have at least 16 characters.");
            return;
        }

        try {
            Account account = accountService.save(username, password);

            if (account.getId() != null) {
                LOGGER.info("Add-account completed successfully for username={}.", LogHelper.maskEmail(username));
                AccountRefreshService.publishAccountsChanged();
                AlertUtil.showInfo("Success", "Account added successfully.");
                stage.close();
                return;
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Add-account failed for username={}.", LogHelper.maskEmail(username), ex);
            AlertUtil.showError("Save Error", "Cannot save account. Please check the local database and try again.");
            return;
        }

        LOGGER.warn("Add-account finished without generated id for username={}.", LogHelper.maskEmail(username));
        AlertUtil.showError("Save Error", "Cannot save account.");
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(regex);
    }
}
