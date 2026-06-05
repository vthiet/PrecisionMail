package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import jakarta.mail.MessagingException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import nlu.fit.soft.gr5.precisionMail.controller.dialog.PreviewEmailController;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.AccountRefreshService;
import nlu.fit.soft.gr5.precisionMail.service.ApplicationStateService;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.LoadAccountService;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;
import nlu.fit.soft.gr5.precisionMail.service.impl.ScheduledEmailServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import nlu.fit.soft.gr5.precisionMail.util.AttachmentValidator;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.KeyboardShortcutUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComposeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComposeController.class);
    private static final String INVALID_STYLE = "-fx-border-color: #dc2626; -fx-border-width: 1.2;";
    private static final String EMPTY_EDITOR_HTML =
            "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";

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
    public HTMLEditor contentEditor;
    @FXML
    public Label subjectLabel;
    @FXML
    public MenuButton accountMenuButton;
    @FXML
    public TextField subjectField;
    @FXML
    public Button sendBtn;
    @FXML
    public Button cancelComposeBtn;
    @FXML
    public Button previewBtn;
    @FXML
    public Button importRecipientsBtn;
    @FXML
    public MenuButton attachMenuButton;
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
    private final ScheduledEmailServiceImpl scheduledEmailService = ScheduledEmailServiceImpl.getInstance();
    private final ToggleGroup accountGroup = new ToggleGroup();
    private final ObservableList<File> attachments = FXCollections.observableArrayList();
    private final BooleanProperty scheduleLocked = new SimpleBooleanProperty(false);
    private final BooleanProperty sendLocked = new SimpleBooleanProperty(false);

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
                sendLocked.or(scheduleLocked).or(
                        Bindings.createBooleanBinding(() -> {
                                    boolean hasRecipient =
                                            EmailUtil.hasAnyValidEmail(toField.getText())
                                                    || EmailUtil.hasAnyValidEmail(ccField.getText())
                                                    || EmailUtil.hasAnyValidEmail(bccField.getText());
                                    boolean recipientsValid =
                                            EmailUtil.containsOnlyValidEmails(toField.getText())
                                                    && EmailUtil.containsOnlyValidEmails(ccField.getText())
                                                    && EmailUtil.containsOnlyValidEmails(bccField.getText());
                                    return !(hasRecipient && recipientsValid);
                                },
                                toField.textProperty(),
                                ccField.textProperty(),
                                bccField.textProperty())
                )
        );
        attachMenuButton.disableProperty().bind(sendLocked.or(scheduleLocked));
        scheduleBtn.disableProperty().bind(sendLocked);
        cancelComposeBtn.disableProperty().bind(sendLocked.or(scheduleLocked));

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

        AccountRefreshService.subscribe(this::reloadAccounts);
        setupKeyboardShortcuts();
        reloadAccounts();
    }

    /**
     * Setup keyboard shortcuts for Compose Email UI
     * Shortcuts:
     * - Ctrl + Enter: Send Email
     * - Ctrl + A: Attach File (when not in editor)
     * - Ctrl + Shift + C: Toggle CC field
     * - Ctrl + Shift + B: Toggle BCC field
     * - Escape: Cancel Compose
     */
    private void setupKeyboardShortcuts() {
        // Add key event handler to toField
        toField.setOnKeyPressed(event -> {
            if (KeyboardShortcutUtil.isCtrlEnter(event)) {
                KeyboardShortcutUtil.logShortcutUsed("Ctrl+Enter (Send Email)");
                if (!sendBtn.isDisabled()) {
                    try {
                        handleSendMail(null);
                    } catch (Exception e) {
                        LOGGER.error("Error sending email from keyboard shortcut", e);
                    }
                }
                event.consume();
            } else if (KeyboardShortcutUtil.isCtrlShiftC(event)) {
                KeyboardShortcutUtil.logShortcutUsed("Ctrl+Shift+C (Toggle CC)");
                if (ccBtn.isVisible()) {
                    toggleCc(null);
                }
                event.consume();
            } else if (KeyboardShortcutUtil.isCtrlShiftB(event)) {
                KeyboardShortcutUtil.logShortcutUsed("Ctrl+Shift+B (Toggle BCC)");
                if (bccBtn.isVisible()) {
                    toggleBcc(null);
                }
                event.consume();
            } else if (KeyboardShortcutUtil.isEscape(event)) {
                KeyboardShortcutUtil.logShortcutUsed("Escape (Cancel Compose)");
                handleCancelCompose(null);
                event.consume();
            }
        });

        // Add key event handler to subjectField
        subjectField.setOnKeyPressed(event -> {
            if (KeyboardShortcutUtil.isCtrlEnter(event)) {
                KeyboardShortcutUtil.logShortcutUsed("Ctrl+Enter (Send Email)");
                if (!sendBtn.isDisabled()) {
                    try {
                        handleSendMail(null);
                    } catch (Exception e) {
                        LOGGER.error("Error sending email from keyboard shortcut", e);
                    }
                }
                event.consume();
            } else if (KeyboardShortcutUtil.isCtrlA(event) && !event.isShiftDown()) {
                // Allow Ctrl+A in text field for selecting all
            }
        });

        // Add key event handler to ccField
        ccField.setOnKeyPressed(event -> {
            if (KeyboardShortcutUtil.isCtrlEnter(event)) {
                KeyboardShortcutUtil.logShortcutUsed("Ctrl+Enter (Send Email)");
                if (!sendBtn.isDisabled()) {
                    try {
                        handleSendMail(null);
                    } catch (Exception e) {
                        LOGGER.error("Error sending email from keyboard shortcut", e);
                    }
                }
                event.consume();
            }
        });

        // Add key event handler to bccField
        bccField.setOnKeyPressed(event -> {
            if (KeyboardShortcutUtil.isCtrlEnter(event)) {
                KeyboardShortcutUtil.logShortcutUsed("Ctrl+Enter (Send Email)");
                if (!sendBtn.isDisabled()) {
                    try {
                        handleSendMail(null);
                    } catch (Exception e) {
                        LOGGER.error("Error sending email from keyboard shortcut", e);
                    }
                }
                event.consume();
            }
        });

        // Set button tooltips to show keyboard shortcuts
        sendBtn.setTooltip(new Tooltip("Send Email (Ctrl+Enter)"));
        previewBtn.setTooltip(new Tooltip("Preview Email before sending"));
        cancelComposeBtn.setTooltip(new Tooltip("Cancel Compose (Esc)"));
        ccBtn.setTooltip(new Tooltip("Toggle CC field (Ctrl+Shift+C)"));
        bccBtn.setTooltip(new Tooltip("Toggle BCC field (Ctrl+Shift+B)"));

        LOGGER.info("Keyboard shortcuts initialized successfully for Compose Email UI");
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

        if (!validateRecipientFields()) {
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

        if (isBlankEmail(email) && !confirmBlankEmail()) {
            LOGGER.info("Send email cancelled by user after blank-content confirmation.");
            return;
        }

        setSendLocked(true);
        ApplicationStateService.beginEmailSend();
        emailService.sendAsync(currentAccount, email)
                .whenComplete((result, throwable) -> Platform.runLater(() -> handleSendCompleted(result, throwable)));

        LOGGER.info(
                "Email handoff to async sender completed. sender={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(currentAccount.getUsername()),
                LogHelper.recipientCount(email),
                LogHelper.attachmentCount(email)
        );
    }

    private void updateMenu(List<Account> accounts) {
        String selectedUsername = currentAccount != null ? currentAccount.getUsername() : null;
        currentAccount = null;
        accountMenuButton.getItems().clear();
        accountGroup.getToggles().clear();

        for (Account account : accounts) {
            RadioMenuItem item = new RadioMenuItem(accountMenuLabel(account));
            item.setUserData(account.getUsername());
            item.setToggleGroup(accountGroup);
            item.setOnAction(e -> {
                currentAccount = account;
                accountMenuButton.setText(accountButtonLabel(account));
                LOGGER.info("Active account changed to username={}.", LogHelper.maskEmail(account.getUsername()));
            });

            accountMenuButton.getItems().add(item);

            if (selectedUsername != null && selectedUsername.equals(account.getUsername())) {
                item.setSelected(true);
                currentAccount = account;
                accountMenuButton.setText(accountButtonLabel(account));
            }
        }

        if (currentAccount == null && !accounts.isEmpty()) {
            Account defaultAccount = accounts.stream()
                    .filter(Account::isPrimary)
                    .findFirst()
                    .orElse(accounts.getFirst());
            currentAccount = defaultAccount;
            accountMenuButton.setText(accountButtonLabel(defaultAccount));
            accountGroup.getToggles().stream()
                    .filter(toggle -> toggle instanceof RadioMenuItem item
                            && defaultAccount.getUsername().equals(item.getUserData()))
                    .findFirst()
                    .ifPresent(toggle -> toggle.setSelected(true));
            LOGGER.info("Default active account set to username={}.", LogHelper.maskEmail(defaultAccount.getUsername()));
        }

        if (currentAccount == null) {
            accountMenuButton.setText("Chọn một tài khoản để gửi");
        }

        accountMenuButton.getItems().add(new SeparatorMenuItem());
        accountMenuButton.getItems().add(new MenuItem("Customize From Address..."));
    }

    private String accountMenuLabel(Account account) {
        String displayName = account.getDisplayName();
        if (displayName.equals(account.getUsername())) {
            return account.isPrimary() ? displayName + " (default)" : displayName;
        }
        String label = displayName + " <" + account.getUsername() + ">";
        return account.isPrimary() ? label + " (default)" : label;
    }

    private String accountButtonLabel(Account account) {
        return account.isPrimary() ? account.getDisplayName() + " (default)" : account.getDisplayName();
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
                LOGGER.info("Attachment accepted. size={} bytes, totalAttachments={}.", file.length(), attachments.size());
            } else {
                LOGGER.warn("Attachment rejected by client-side validation.");
                AlertUtil.showError("Cannot add attachment", validation.errorMessage);
            }
        }
    }

    public void deleteAttachment(File file) {
        attachments.remove(file);
        LOGGER.info("Attachment removed. remainingAttachments={}.", attachments.size());
    }

    private String formatSize(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.1f MB", mb);
    }

    /**
     * Handle Preview Email button click
     * Displays a preview dialog showing From, To, Cc, Bcc, Subject, Body, and Attachments
     */
    public void handlePreviewEmail(ActionEvent actionEvent) {
        if (currentAccount == null) {
            LOGGER.warn("Preview email rejected because no active account is selected.");
            AlertUtil.showError("Error", "Please select an account.");
            return;
        }

        if (!validateRecipientFields()) {
            return;
        }

        Email email = buildEmail(currentAccount);
        if (!hasAnyRecipient(email)) {
            LOGGER.warn("Preview email rejected because recipient list is empty.");
            AlertUtil.showError("Error", "Please enter at least one valid recipient.");
            return;
        }

        try {
            // Load preview dialog FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/nlu/fit/soft/gr5/precisionMail/view/dialog/preview-email.fxml")
            );
            DialogPane previewPane = loader.load();
            PreviewEmailController controller = loader.getController();

            // Populate preview with email data
            controller.setEmail(email, currentAccount.getUsername(), attachments);

            // Create and show dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(previewPane);
            dialog.setTitle("Email Preview");
            dialog.initOwner(sendBtn.getScene().getWindow());

            // Handle Send button from preview dialog
            Button sendButton = (Button) previewPane.lookupAll("Button").stream()
                    .filter(btn -> btn instanceof Button && ((Button) btn).getText().equals("Send"))
                    .findFirst()
                    .orElse(null);
            if (sendButton != null) {
                sendButton.setOnAction(e -> {
                    try {
                        handleSendMail(null);
                    } catch (Exception ex) {
                        LOGGER.error("Error sending email from keyboard shortcut", ex);
                    }
                    dialog.close();
                });
            }

            // Handle Cancel button
            Button cancelButton = (Button) previewPane.lookupAll("Button").stream()
                    .filter(btn -> btn instanceof Button && ((Button) btn).getText().equals("Cancel"))
                    .findFirst()
                    .orElse(null);
            if (cancelButton != null) {
                cancelButton.setOnAction(e -> dialog.close());
            }

            dialog.showAndWait();
            LOGGER.info("Email preview dialog closed. sender={}", LogHelper.maskEmail(currentAccount.getUsername()));

        } catch (Exception e) {
            LOGGER.error("Failed to load preview dialog.", e);
            AlertUtil.showError("Error", "Failed to load preview dialog: " + e.getMessage());
        }
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
        if (scheduledAt.isBefore(LocalDateTime.now().plusSeconds(60))) {
            LOGGER.warn("Schedule email rejected because scheduled time is less than 60 seconds ahead.");
            AlertUtil.showError("Error", "Scheduled time must be at least 60 seconds in the future.");
            return;
        }

        if (!validateRecipientFields()) {
            return;
        }

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
        scheduledEmail.status = EmailStatus.SCHEDULED;

        try {
            scheduledEmail = scheduledEmailService.schedule(scheduledEmail);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(
                    "Schedule request rejected. sender={}, reason={}",
                    LogHelper.maskEmail(currentAccount.getUsername()),
                    ex.getMessage()
            );
            AlertUtil.showError("Error", ex.getMessage());
            return;
        } catch (RuntimeException ex) {
            LOGGER.error("Schedule request failed.", ex);
            AlertUtil.showError("Error", "Cannot save scheduled email. Please try again.");
            return;
        }

        LOGGER.info(
                "Schedule request accepted. taskId={}, sender={}, scheduledAt={}, recipients={}, attachments={}.",
                scheduledEmail.id,
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
        String content = contentEditor.getHtmlText();

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
        clearTextInput(toField, ccField, bccField, subjectField);
        contentEditor.setHtmlText(EMPTY_EDITOR_HTML);
        attachments.clear();
        clearRecipientValidation();
    }

    private boolean hasAnyRecipient(Email email) {
        return LogHelper.recipientCount(email) > 0;
    }

    public void handleImportRecipients(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Recipient files", "*.txt", "*.csv"),
                new FileChooser.ExtensionFilter("Text files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );

        File selectedFile = chooser.showOpenDialog(sendBtn.getScene().getWindow());
        if (selectedFile == null) {
            LOGGER.debug("Recipient import cancelled by user.");
            return;
        }

        Task<Set<String>> importTask = new Task<>() {
            @Override
            protected Set<String> call() throws Exception {
                String content = java.nio.file.Files.readString(selectedFile.toPath());
                return EmailUtil.extractEmails(content);
            }
        };

        importRecipientsBtn.disableProperty().bind(importTask.runningProperty());
        importTask.setOnSucceeded(event -> {
            importRecipientsBtn.disableProperty().unbind();
            Set<String> emails = importTask.getValue();

            if (emails.isEmpty()) {
                LOGGER.warn("Recipient import found no valid email address.");
                AlertUtil.showError("Import Error", "Khong tim thay du lieu hop le.");
                return;
            }

            TextField targetField = bccField.isFocused() ? bccField : toField;
            Set<String> merged = new java.util.LinkedHashSet<>(EmailUtil.emailFeature(targetField.getText()));
            merged.addAll(emails);
            targetField.setText(String.join(", ", merged));
            LOGGER.info("Recipient import completed successfully. count={}", emails.size());
            AlertUtil.showInfo("Import Success", "Imported " + emails.size() + " recipient(s).");
        });
        importTask.setOnFailed(event -> {
            importRecipientsBtn.disableProperty().unbind();
            LOGGER.error("Recipient import failed.", importTask.getException());
            AlertUtil.showError("Import Error", "Cannot read recipient file.");
        });

        Thread thread = new Thread(importTask, "recipient-import-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private void reloadAccounts() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::reloadAccounts);
            return;
        }

        if (loadAccountService.isRunning()) {
            return;
        }

        if (loadAccountService.getState() == Worker.State.READY) {
            loadAccountService.start();
            return;
        }

        loadAccountService.restart();
    }

    private boolean validateRecipientFields() {
        clearRecipientValidation();
        Optional<String> invalidTo = firstInvalidEmail(toField.getText());
        Optional<String> invalidCc = firstInvalidEmail(ccField.getText());
        Optional<String> invalidBcc = firstInvalidEmail(bccField.getText());

        if (invalidTo.isPresent() || invalidCc.isPresent() || invalidBcc.isPresent()) {
            Optional<String> invalid = invalidTo.or(() -> invalidCc).or(() -> invalidBcc);
            invalidTo.ifPresent(value -> markInvalid(toField, value));
            invalidCc.ifPresent(value -> markInvalid(ccField, value));
            invalidBcc.ifPresent(value -> markInvalid(bccField, value));

            LOGGER.warn("Recipient validation failed because at least one address is invalid.");
            AlertUtil.showError("Error", "Địa chỉ email [" + invalid.orElse("") + "] không hợp lệ. Vui lòng kiểm tra lại.");
            return false;
        }
        return true;
    }

    private void setScheduleLocked(boolean locked) {
        scheduleLocked.set(locked);
        toField.setDisable(locked);
        ccField.setDisable(locked);
        bccField.setDisable(locked);
        subjectField.setDisable(locked);
        contentEditor.setDisable(locked);
        accountMenuButton.setDisable(locked);
        scheduleDatePicker.setDisable(locked);
        hourBox.setDisable(locked);
        minuteBox.setDisable(locked);
        if (!importRecipientsBtn.disableProperty().isBound()) {
            importRecipientsBtn.setDisable(locked);
        }
        scheduleBtn.setText(locked ? "Cancel schedule" : "Schedule send");
    }

    public void handleCancelCompose(ActionEvent actionEvent) {
        if (!hasDraftContent() || confirmDiscardDraft()) {
            clearComposeForm();
            LOGGER.info("Compose draft discarded by user.");
        }
    }

    private void handleSendCompleted(EmailService.SendResult result, Throwable throwable) {
        ApplicationStateService.endEmailSend();
        setSendLocked(false);
        if (throwable != null || result == null || !result.success()) {
            Throwable error = throwable != null ? throwable : result.error();
            LOGGER.warn("Async send completed with failure.", error);
            AlertUtil.showError("Send failed", userMessageForSendFailure(error));
            return;
        }

        clearComposeForm();
        AlertUtil.showInfo("Success", "Email đã được gửi thành công.");
    }

    private void setSendLocked(boolean locked) {
        sendLocked.set(locked);
        if (!importRecipientsBtn.disableProperty().isBound()) {
            importRecipientsBtn.setDisable(locked);
        }
    }

    private Optional<String> firstInvalidEmail(String plainText) {
        return EmailUtil.emailFeature(plainText).stream()
                .filter(email -> !EmailUtil.isValidEmail(email))
                .findFirst();
    }

    private void markInvalid(TextInputControl field, String invalidEmail) {
        field.setStyle(INVALID_STYLE);
        field.setTooltip(new Tooltip("Địa chỉ email [" + invalidEmail + "] không hợp lệ."));
    }

    private void clearRecipientValidation() {
        toField.setStyle(null);
        ccField.setStyle(null);
        bccField.setStyle(null);
        toField.setTooltip(null);
        ccField.setTooltip(null);
        bccField.setTooltip(null);
    }

    private boolean isBlankEmail(Email email) {
        return (email.subject == null || email.subject.isBlank()) && htmlBodyIsBlank(email.content);
    }

    private boolean hasDraftContent() {
        return !toField.getText().isBlank()
                || !ccField.getText().isBlank()
                || !bccField.getText().isBlank()
                || !subjectField.getText().isBlank()
                || !htmlBodyIsBlank(contentEditor.getHtmlText())
                || !attachments.isEmpty();
    }

    private boolean htmlBodyIsBlank(String html) {
        if (html == null || html.isBlank()) {
            return true;
        }
        String withoutTags = html.replaceAll("(?is)<style.*?</style>", "")
                .replaceAll("(?is)<script.*?</script>", "")
                .replaceAll("(?is)<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", "");
        return withoutTags.isBlank();
    }

    private boolean confirmBlankEmail() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm send");
        alert.setHeaderText(null);
        alert.setContentText("Tiêu đề và nội dung đang trống. Bạn có chắc chắn muốn gửi?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private boolean confirmDiscardDraft() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hủy soạn");
        alert.setHeaderText(null);
        alert.setContentText("Nội dung thư đang soạn thảo sẽ bị mất. Bạn có chắc chắn muốn thoát?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private String userMessageForSendFailure(Throwable error) {
        if (isNetworkFailure(error)) {
            return "Gửi email thất bại. Vui lòng kiểm tra kết nối Internet của máy trạm.";
        }
        String detail = error == null || error.getMessage() == null || error.getMessage().isBlank()
                ? "Không rõ nguyên nhân"
                : error.getMessage();
        return "Gửi thư thất bại. Mail Server phản hồi lỗi: " + detail;
    }

    private boolean isNetworkFailure(Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            String name = cause.getClass().getName();
            if (name.equals("java.net.UnknownHostException")
                    || name.equals("java.net.ConnectException")
                    || name.equals("java.net.SocketTimeoutException")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

}
