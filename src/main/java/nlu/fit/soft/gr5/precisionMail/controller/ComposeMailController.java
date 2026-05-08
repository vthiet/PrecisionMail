package nlu.fit.soft.gr5.precisionMail.controller;

import jakarta.mail.MessagingException;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.LoadAccountService;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;

import java.util.List;

public class ComposeMailController {

    private final EmailService emailService = new EmailServiceImpl();
    private final LoadAccountService loadAccountService = new LoadAccountService();
    private ToggleGroup accountGroup = new ToggleGroup();

    @FXML
    public Label ccLabel;
    @FXML
    public TextField ccField;
    @FXML
    public Label bccLabel;
    @FXML
    public TextField bccField;
    @FXML
    public Button ccBtn;
    @FXML
    public Button bccBtn;
    @FXML
    public Label attachmentSizeLabel;
    @FXML
    public TextField toField;
    @FXML
    public TextArea contentArea;
    @FXML
    public Label subjectLabel;
    @FXML
    public MenuButton accountMenuButton;

    @FXML
    public void initialize() {
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

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(15, 15);

        loadAccountService.runningProperty().addListener(
                (obs, oldValue, running) -> {

                    if (running) {
                        accountMenuButton.setGraphic(loadingIndicator);
                    } else {
                        accountMenuButton.setGraphic(null);
                    }
                });

        loadAccountService.setOnSucceeded(e -> {
            updateMenu(loadAccountService.getValue());
        });

        loadAccountService.setOnFailed(e -> {

            accountMenuButton.setText("Tai tai khoan that bai.");

            loadAccountService.getException().printStackTrace();
        });
    }

    @FXML
    public void toggleCc(ActionEvent actionEvent) {
        ccBtn.setVisible(false);
    }

    @FXML
    public void toggleBcc(ActionEvent actionEvent) {
        bccBtn.setVisible(false);
    }

    public void handleSendMail(ActionEvent actionEvent) throws MessagingException {
        String to = toField.getText();
        String subject = subjectLabel.getText();
        String content = contentArea.getText();

        emailService.send(to, subject, content);
    }

    public void handleLoadAccounts(Event event) {
        if (!loadAccountService.isRunning()) loadAccountService.restart();
    }

    private void updateMenu(List<Account> accounts) {
        accountMenuButton.getItems().clear();

        for (Account account : accounts) {
            RadioMenuItem item = new RadioMenuItem(account.getUsername());
            item.setToggleGroup(accountGroup);

            item.setOnAction(e -> {
                accountMenuButton.setText(account.getUsername());
            });

            accountMenuButton.getItems().add(item);
        }

        accountMenuButton.getItems().add(new SeparatorMenuItem());
        accountMenuButton.getItems().add(new MenuItem("Customize From Address..."));
    }
}