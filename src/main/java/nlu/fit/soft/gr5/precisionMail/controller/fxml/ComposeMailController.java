package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import jakarta.mail.MessagingException;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.LoadAccountService;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ComposeMailController {
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
    public TextField subjectField;
    @FXML
    public Button sendBtn;
    @FXML
    public VBox attachmentContainer;
    @FXML
    public ListView<File> attachmentListView;
    @FXML
    public Label attachmentCountLabel;

    private final EmailService emailService = new EmailServiceImpl();
    private final LoadAccountService loadAccountService = new LoadAccountService();
    private final ToggleGroup accountGroup = new ToggleGroup();


    private Account currentAccount = null;
    private final ObservableList<File> attachments = FXCollections.observableArrayList();

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

        attachmentContainer.visibleProperty().bind(Bindings.isNotEmpty(attachments));
        attachmentContainer.managedProperty().bind(attachmentContainer.visibleProperty());

        attachmentListView.setItems(attachments);

        attachmentCountLabel.textProperty().bind(
                Bindings.size(attachments)
                        .asString("%d Attachment(s)")
        );

        attachmentSizeLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {

                    long total = attachments.stream()
                            .mapToLong(File::length)
                            .sum();

                    return formatSize(total);

                }, attachments)
        );

        sendBtn.disableProperty().bind(
                Bindings.createBooleanBinding(() -> {
                            boolean toValid = EmailUtil.isValidEmail(toField.getText());
                            boolean ccValid = EmailUtil.isValidEmail(ccField.getText());
                            boolean bccValid = EmailUtil.isValidEmail(bccField.getText());
                            return !(toValid || ccValid || bccValid);
                        },
                        toField.textProperty(),
                        ccField.textProperty(),
                        bccField.textProperty())
        );

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(15, 15);

        loadAccountService.runningProperty().addListener((obs, oldValue, running) -> {
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
            accountMenuButton.setText("Tải tài khoản thất bại.");
            loadAccountService.getException().printStackTrace();
        });

        loadAccountService.start();
    }

    @FXML
    public void toggleCc(ActionEvent actionEvent) {
        ccBtn.setVisible(false);
    }

    @FXML
    public void toggleBcc(ActionEvent actionEvent) {
        bccBtn.setVisible(false);
    }

    public void handleSendMail(ActionEvent actionEvent) throws MessagingException, IOException {
        Account account = currentAccount;

        if (currentAccount == null) {
            System.err.println("Please select account.");
            return;
        }

        Set<String> toLst = EmailUtil.emailFeature(toField.getText());
        Set<String> ccLst = EmailUtil.emailFeature(ccField.getText());
        Set<String> bccLst = EmailUtil.emailFeature(bccField.getText());
        String subject = subjectField.getText();
        String content = contentArea.getText();

        // Handle
        Email email = new Email(account.getUsername(),
                toLst, ccLst, bccLst, subject, content, null, LocalDateTime.now());

        emailService.send(account, email);

        clearTextInput(toField, ccField, bccField, subjectField, contentArea);
    }

    private void updateMenu(List<Account> accounts) {
        boolean first = true;
        accountMenuButton.getItems().clear();

        for (Account account : accounts) {
            RadioMenuItem item = new RadioMenuItem(account.getUsername());
            item.setToggleGroup(accountGroup);

            item.setOnAction(e -> {
                currentAccount = account;
                accountMenuButton.setText(account.getUsername());
            });

            accountMenuButton.getItems().add(item);

            if (first) {
                item.setSelected(true);
                currentAccount = account;
                accountMenuButton.setText(account.getUsername());
                first = false;
            }
        }

        accountMenuButton.getItems().add(new SeparatorMenuItem());
        accountMenuButton.getItems().add(new MenuItem("Customize From Address..."));
    }

    private void clearTextInput(TextInputControl... inputLst) {
        if (inputLst != null) {
            for (var input : inputLst) {
                input.clear();
            }
        }
    }

    public void handleAttachFiles(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(
                attachmentContainer.getScene().getWindow()
        );

        if (files != null) attachments.addAll(files);
    }

    private String formatSize(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.1f MB", mb);
    }
}