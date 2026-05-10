package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import jakarta.mail.MessagingException;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.LoadAccountService;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;
import nlu.fit.soft.gr5.precisionMail.service.impl.ScheduledEmailServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import nlu.fit.soft.gr5.precisionMail.util.AttachmentValidator;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ComposeMailController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComposeMailController.class);

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
    @FXML
    public DatePicker scheduleDatePicker;
    @FXML
    public ComboBox<Integer> hourBox;
    @FXML
    public ComboBox<Integer> minuteBox;
    @FXML
    public Button scheduleBtn;

    private final EmailService emailService = new EmailServiceImpl();
    private final LoadAccountService loadAccountService = new LoadAccountService();
    private final ScheduledEmailServiceImpl scheduledEmailService = new ScheduledEmailServiceImpl();
    private final ToggleGroup accountGroup = new ToggleGroup();
    private final ObservableList<File> attachments = FXCollections.observableArrayList();

    private Account currentAccount = null;

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

        for (int i = 0; i < 24; i++) {
            hourBox.getItems().add(i);
        }

        for (int i = 0; i < 60; i++) {
            minuteBox.getItems().add(i);
        }

        hourBox.setValue(LocalTime.now().getHour());
        minuteBox.setValue(LocalTime.now().getMinute());
        scheduleDatePicker.setValue(LocalDate.now());

        attachmentListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);

                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(10);
                hbox.setPadding(new Insets(5));
                hbox.setStyle("-fx-alignment: CENTER_LEFT;");

                Label fileLabel = new Label(file.getName());
                fileLabel.setStyle("-fx-text-fill: #333333;");

                Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button deleteBtn = new Button("x");
                deleteBtn.setPrefSize(25, 25);
                deleteBtn.setStyle("-fx-font-size: 16; -fx-padding: 0; -fx-text-fill: #d9534f;");
                deleteBtn.setOnAction(e -> deleteAttachment(file));

                hbox.getChildren().addAll(fileLabel, spacer, deleteBtn);
                setGraphic(hbox);
            }
        });

        attachmentCountLabel.textProperty().bind(
                Bindings.size(attachments).asString("%d Attachment(s)")
        );

        attachmentSizeLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    long total = attachments.stream().mapToLong(File::length).sum();
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
            LOGGER.info("Account menu loaded successfully.");
            updateMenu(loadAccountService.getValue());
        });

        loadAccountService.setOnFailed(e -> {
            accountMenuButton.setText("Load account failed.");
            LOGGER.error("Account menu loading failed.", loadAccountService.getException());
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
        if (currentAccount == null) {
            LOGGER.warn("Send email rejected because no active account is selected.");
            AlertUtil.showError("Error", "Please select an account before sending email.");
            return;
        }

        Email email = buildEmail(currentAccount);
        if (!hasAnyRecipient(email)) {
            LOGGER.warn("Send email rejected because recipient list is empty.");
            AlertUtil.showError("Error", "Please enter at least one valid recipient.");
            return;
        }

        AttachmentValidator.ValidationResult attachmentValidation =
                AttachmentValidator.validateAttachmentList(attachments);
        if (!attachmentValidation.isValid) {
            LOGGER.warn("Send email rejected because attachments are invalid. reason={}", attachmentValidation.errorMessage);
            AlertUtil.showError("Error", attachmentValidation.errorMessage);
            return;
        }

        emailService.send(currentAccount, email);

        LOGGER.info(
                "Email handoff to async sender completed. sender={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(currentAccount.getUsername()),
                LogHelper.recipientCount(email),
                LogHelper.attachmentCount(email)
        );

        clearComposeForm();
        AlertUtil.showInfo("Processing", "Email send request was submitted to the background worker.");
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
                LOGGER.info("Active account changed to username={}.", LogHelper.maskEmail(account.getUsername()));
            });

            accountMenuButton.getItems().add(item);

            if (first) {
                item.setSelected(true);
                currentAccount = account;
                accountMenuButton.setText(account.getUsername());
                LOGGER.info("Default active account set to username={}.", LogHelper.maskEmail(account.getUsername()));
                first = false;
            }
        }

        accountMenuButton.getItems().add(new SeparatorMenuItem());
        accountMenuButton.getItems().add(new MenuItem("Customize From Address..."));
    }

    private void clearTextInput(TextInputControl... inputLst) {
        if (inputLst == null) {
            return;
        }

        for (var input : inputLst) {
            input.clear();
        }
    }

    public void handleAttachFiles(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(
                attachmentContainer.getScene().getWindow()
        );

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            LOGGER.debug("Attachment selection cancelled by user.");
            return;
        }

        for (File file : selectedFiles) {
            AttachmentValidator.ValidationResult validation =
                    AttachmentValidator.validateFileAddition(file, attachments);

            if (validation.isValid) {
                attachments.add(file);
                LOGGER.info("Attachment accepted. name={}, size={} bytes.", file.getName(), file.length());
            } else {
                LOGGER.warn("Attachment rejected. name={}, reason={}", file.getName(), validation.errorMessage);
                AlertUtil.showError("Cannot add attachment", validation.errorMessage);
            }
        }
    }

    public void deleteAttachment(File file) {
        attachments.remove(file);
        LOGGER.info("Attachment removed. name={}", file.getName());
    }

    private String formatSize(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.1f MB", mb);
    }

    public void handleScheduleSendMail(ActionEvent actionEvent) {
        if (currentAccount == null) {
            LOGGER.warn("Schedule email rejected because no active account is selected.");
            AlertUtil.showError("Error", "Please select an account before scheduling email.");
            return;
        }

        LocalDate date = scheduleDatePicker.getValue();
        Integer hour = hourBox.getValue();
        Integer minute = minuteBox.getValue();

        if (date == null || hour == null || minute == null) {
            LOGGER.warn("Schedule email rejected because schedule time is incomplete.");
            AlertUtil.showError("Error", "Please choose a complete schedule time.");
            return;
        }

        LocalDateTime scheduledAt = LocalDateTime.of(date, LocalTime.of(hour, minute));
        Email email = buildEmail(currentAccount);
        if (!hasAnyRecipient(email)) {
            LOGGER.warn("Schedule email rejected because recipient list is empty.");
            AlertUtil.showError("Error", "Please enter at least one valid recipient.");
            return;
        }

        AttachmentValidator.ValidationResult attachmentValidation =
                AttachmentValidator.validateAttachmentList(attachments);
        if (!attachmentValidation.isValid) {
            LOGGER.warn("Schedule email rejected because attachments are invalid. reason={}", attachmentValidation.errorMessage);
            AlertUtil.showError("Error", attachmentValidation.errorMessage);
            return;
        }

        ScheduledEmail scheduledEmail = new ScheduledEmail(currentAccount, email, scheduledAt);

        try {
            scheduledEmailService.schedule(scheduledEmail);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(
                    "Schedule request rejected. sender={}, reason={}",
                    LogHelper.maskEmail(currentAccount.getUsername()),
                    ex.getMessage()
            );
            AlertUtil.showError("Error", "Scheduled time must be in the future.");
            return;
        }

        LOGGER.info(
                "Schedule request accepted. sender={}, scheduledAt={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(currentAccount.getUsername()),
                scheduledAt,
                LogHelper.recipientCount(email),
                LogHelper.attachmentCount(email)
        );

        clearComposeForm();
        AlertUtil.showInfo("Success", "Email was scheduled successfully.");
    }

    private Email buildEmail(Account account) {
        Set<String> toLst = EmailUtil.emailFeature(toField.getText());
        Set<String> ccLst = EmailUtil.emailFeature(ccField.getText());
        Set<String> bccLst = EmailUtil.emailFeature(bccField.getText());
        String subject = subjectField.getText();
        String content = contentArea.getText();

        List<String> attachmentPaths = attachments.isEmpty()
                ? null
                : attachments.stream().map(File::getAbsolutePath).collect(Collectors.toList());

        return new Email(
                account.getUsername(),
                toLst,
                ccLst,
                bccLst,
                subject,
                content,
                attachmentPaths,
                LocalDateTime.now()
        );
    }

    private void clearComposeForm() {
        clearTextInput(toField, ccField, bccField, subjectField, contentArea);
        attachments.clear();
    }

    private boolean hasAnyRecipient(Email email) {
        return LogHelper.recipientCount(email) > 0;
    }
}
