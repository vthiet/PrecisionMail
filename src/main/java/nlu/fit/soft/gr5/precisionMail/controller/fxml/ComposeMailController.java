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
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
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
import nlu.fit.soft.gr5.precisionMail.service.AccountRefreshService;
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
import java.util.concurrent.ScheduledFuture;
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
    public Button importRecipientsBtn;
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
    private final BooleanProperty scheduleLocked = new SimpleBooleanProperty(false);

    private Account currentAccount = null;
    private ScheduledFuture<?> pendingScheduledJob;

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

        // 2.1.2. Hệ thống vô hiệu hóa chức năng "Gửi" cho đến khi các địa chỉ người nhận được nhập hợp lệ.
        // 2.1.4. Hệ thống tự động kiểm tra định dạng email sau mỗi thay đổi.
        // Nếu tất cả email đúng định dạng và có ít nhất một người nhận, chức năng "Gửi" sẽ được kích hoạt.
        sendBtn.disableProperty().bind(
                scheduleLocked.or(
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
        reloadAccounts();
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

        // Post-Condition: Thông tin Email được đóng gói vào đối tượng Email và sẵn sàng để gửi
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

        // Post-Condition: Thông tin Email được đóng gói vào đối tượng Email và sẵn sàng để gửi
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
        String selectedUsername = currentAccount != null ? currentAccount.getUsername() : null;
        currentAccount = null;
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

            if (selectedUsername != null && selectedUsername.equals(account.getUsername())) {
                item.setSelected(true);
                currentAccount = account;
                accountMenuButton.setText(account.getUsername());
            }
        }

        // 2.1.1. Hệ thống hiển thị giao diện soạn thảo.
        // Trường "Người gửi" (From) tự động hiển thị thông tin của tài khoản đầu tiên trong danh sách tài khoản đã lưu và có thể chọn các account trong dropdown đã được lưu vào Database làm From Account.
        if (currentAccount == null && !accounts.isEmpty()) {
            Account firstAccount = accounts.getFirst();
            currentAccount = firstAccount;
            accountMenuButton.setText(firstAccount.getUsername());
            accountGroup.getToggles().stream()
                    .filter(toggle -> toggle instanceof RadioMenuItem item
                            && firstAccount.getUsername().equals(item.getText()))
                    .findFirst()
                    .ifPresent(toggle -> toggle.setSelected(true));
            LOGGER.info("Default active account set to username={}.", LogHelper.maskEmail(firstAccount.getUsername()));
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

    // 2.1.7. Hệ thống kiểm tra tính hợp lệ của tệp dựa trên: sự tồn tại, định dạng an toàn, số lượng tệp và tổng dung lượng.
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

            // 2.1.8. Nếu tệp hợp lệ, hệ thống hiển thị tệp trong danh sách đính kèm và cập nhật tổng dung lượng hiển thị trên giao diện.
            if (validation.isValid) {
                attachments.add(file);
                LOGGER.info("Attachment accepted. name={}, size={} bytes.", file.getName(), file.length());
            } else {
                LOGGER.warn("Attachment rejected. name={}, reason={}", file.getName(), validation.errorMessage);
                AlertUtil.showError("Cannot add attachment", validation.errorMessage);
            }
        }
    }

    // 2.2.1. Xóa tệp đính kèm (Từ bước 2.1.8): Người dùng nhấn nút xóa bên cạnh tệp đã đính kèm.
    // Hệ thống loại bỏ tệp khỏi danh sách và cập nhật lại thông số dung lượng/số lượng.
    public void deleteAttachment(File file) {
        attachments.remove(file);
        LOGGER.info("Attachment removed. name={}", file.getName());
    }

    private String formatSize(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.1f MB", mb);
    }

    public void handleScheduleSendMail(ActionEvent actionEvent) {
        if (pendingScheduledJob != null && !pendingScheduledJob.isDone()) {
            pendingScheduledJob.cancel(false);
            pendingScheduledJob = null;
            setScheduleLocked(false);
            LOGGER.info("Pending scheduled email cancelled by user.");
            AlertUtil.showInfo("Cancelled", "Scheduled email was cancelled.");
            return;
        }

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
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            LOGGER.warn("Schedule email rejected because scheduled time is in the past.");
            AlertUtil.showError("Error", "Scheduled time must be in the future.");
            return;
        }

        if (scheduledAt.isAfter(LocalDateTime.now().plusHours(2))) {
            LOGGER.warn("Schedule email rejected because scheduled time is more than 2 hours ahead.");
            AlertUtil.showError("Error", "Scheduled time must be within the next 2 hours.");
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

        try {
            pendingScheduledJob = scheduledEmailService.scheduleJob(scheduledEmail);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(
                    "Schedule request rejected. sender={}, reason={}",
                    LogHelper.maskEmail(currentAccount.getUsername()),
                    ex.getMessage()
            );
            AlertUtil.showError("Error", "Scheduled time must be in the future.");
            return;
        }

        setScheduleLocked(true);
        watchScheduledJobCompletion(pendingScheduledJob);

        LOGGER.info(
                "Schedule request accepted. sender={}, scheduledAt={}, recipients={}, attachments={}.",
                LogHelper.maskEmail(currentAccount.getUsername()),
                scheduledAt,
                LogHelper.recipientCount(email),
                LogHelper.attachmentCount(email)
        );

        clearComposeForm();
        AlertUtil.showInfo("Success", "Email was scheduled successfully. Use the same button to cancel before send time.");
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
                LOGGER.warn("Recipient import found no valid email address. file={}", selectedFile.getAbsolutePath());
                AlertUtil.showError("Import Error", "Khong tim thay du lieu hop le.");
                return;
            }

            TextField targetField = bccField.isFocused() ? bccField : toField;
            Set<String> merged = new java.util.LinkedHashSet<>(EmailUtil.emailFeature(targetField.getText()));
            merged.addAll(emails);
            targetField.setText(String.join(", ", merged));
            LOGGER.info("Recipient import completed successfully. file={}, count={}", selectedFile.getName(), emails.size());
            AlertUtil.showInfo("Import Success", "Imported " + emails.size() + " recipient(s).");
        });
        importTask.setOnFailed(event -> {
            importRecipientsBtn.disableProperty().unbind();
            LOGGER.error("Recipient import failed. file={}", selectedFile.getAbsolutePath(), importTask.getException());
            AlertUtil.showError("Import Error", "Cannot read recipient file.");
        });

        Thread thread = new Thread(importTask, "recipient-import-worker");
        thread.setDaemon(true);
        thread.start();
    }

    private void reloadAccounts() {
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
        if (!EmailUtil.containsOnlyValidEmails(toField.getText())
                || !EmailUtil.containsOnlyValidEmails(ccField.getText())
                || !EmailUtil.containsOnlyValidEmails(bccField.getText())) {
            LOGGER.warn("Recipient validation failed because at least one address is invalid.");
            AlertUtil.showError("Error", "One or more recipient addresses are invalid.");
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
        contentArea.setDisable(locked);
        accountMenuButton.setDisable(locked);
        scheduleDatePicker.setDisable(locked);
        hourBox.setDisable(locked);
        minuteBox.setDisable(locked);
        if (!importRecipientsBtn.disableProperty().isBound()) {
            importRecipientsBtn.setDisable(locked);
        }
        scheduleBtn.setText(locked ? "Cancel schedule" : "Schedule send");
    }

    private void watchScheduledJobCompletion(ScheduledFuture<?> job) {
        Thread watcher = new Thread(() -> {
            try {
                job.get();
            } catch (Exception ignored) {
                // The scheduled worker already logs the failure details.
            } finally {
                Platform.runLater(() -> {
                    if (pendingScheduledJob == job) {
                        pendingScheduledJob = null;
                    }
                    setScheduleLocked(false);
                });
            }
        }, "scheduled-email-ui-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }
}
