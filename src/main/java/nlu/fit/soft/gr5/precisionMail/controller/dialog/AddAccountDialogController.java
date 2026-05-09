package nlu.fit.soft.gr5.precisionMail.controller.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;

public class AddAccountDialogController {
    @FXML
    public TextField usernameField;
    @FXML
    public TextField passwordField;
    private Stage stage;

    private final AccountService accountService = new AccountServiceImpl();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleCancel(ActionEvent actionEvent) {
        stage.close();
    }

    public void handleSave(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            AlertUtil.showError("Validation Error", "Email and Password cannot be empty.");
            return;
        }

        if (!isValidEmail(username)){
            AlertUtil.showError("Validation Error", "Email invalid.");
        }

        Account account = accountService.save(username, password);

        if (account.getId() != null){
            AlertUtil.showInfo("Success", "Account added successfully.");
            stage.close();
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(regex);
    }

}
