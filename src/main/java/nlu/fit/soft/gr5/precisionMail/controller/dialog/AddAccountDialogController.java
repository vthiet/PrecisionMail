package nlu.fit.soft.gr5.precisionMail.controller.dialog;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import nlu.fit.soft.gr5.precisionMail.service.AccountRefreshService;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AddAccountDialogController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddAccountDialogController.class);
    private static final String INVALID_STYLE = "-fx-border-color: #dc2626; -fx-border-width: 1.2;";

    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField smtpHostField;
    @FXML
    public TextField smtpPortField;
    @FXML
    public TextField imapHostField;
    @FXML
    public TextField imapPortField;
    @FXML
    public ComboBox<SecurityMode> securityModeComboBox;
    @FXML
    public Button testConnectionButton;
    @FXML
    public Button saveButton;
    @FXML
    public Button cancelButton;
    @FXML
    public ProgressIndicator progressIndicator;
    @FXML
    public Label statusLabel;

    private Stage stage;
    private final AccountService accountService = new AccountServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private Account loadedAccount;
    private String initialFingerprint = "";
    private boolean connectionValidated;
    private boolean closingAfterSave;
    private boolean loadingConfiguration;

    @FXML
    public void initialize() {
        securityModeComboBox.getItems().setAll(SecurityMode.values());
        securityModeComboBox.setValue(SecurityMode.TLS);
        securityModeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (loadingConfiguration) {
                return;
            }
            applySuggestedPorts(newValue);
            markConnectionDirty();
        });

        loadExistingConfiguration();
        bindDirtyTracking();
        setTesting(false);
        saveButton.setDisable(true);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(this::handleCloseRequest);
    }

    public void handleCancel(ActionEvent actionEvent) {
        requestClose();
    }

    public void handleTestConnection(ActionEvent actionEvent) {
        Account account = buildAccountFromForm();
        if (!validateForm(account)) {
            saveButton.setDisable(true);
            connectionValidated = false;
            return;
        }

        LOGGER.info("Mail-server test requested for username={}.", LogHelper.maskEmail(account.getUsername()));
        setTesting(true);
        statusLabel.setText("Đang kiểm tra kết nối...");

        CompletableFuture
                .runAsync(() -> {
                    try {
                        emailService.validateConnection(account);
                    } catch (MessagingException ex) {
                        throw new CompletionException(ex);
                    }
                }, AppExecutors.io())
                .whenComplete((unused, throwable) -> Platform.runLater(() -> handleTestCompleted(account, throwable)));
    }

    public void handleSave(ActionEvent actionEvent) {
        Account account = buildAccountFromForm();
        if (!validateForm(account)) {
            return;
        }
        if (!connectionValidated) {
            AlertUtil.showError("Validation Error", "Vui lòng kiểm tra kết nối thành công trước khi lưu cấu hình.");
            return;
        }

        setTesting(true);
        statusLabel.setText("Đang lưu cấu hình...");

        CompletableFuture
                .supplyAsync(() -> accountService.save(account), AppExecutors.io())
                .whenComplete((savedAccount, throwable) -> Platform.runLater(() -> handleSaveCompleted(savedAccount, throwable)));
    }

    private void loadExistingConfiguration() {
        loadingConfiguration = true;
        try {
            loadedAccount = accountService.findPrimaryConfiguration();
            if (loadedAccount == null) {
                MailServerConfig defaults = new MailServerConfig();
                smtpHostField.setText(defaults.getSmtpHost());
                smtpPortField.setText(String.valueOf(defaults.getSmtpPort()));
                imapHostField.setText(defaults.getImapHost());
                imapPortField.setText(String.valueOf(defaults.getImapPort()));
                securityModeComboBox.setValue(defaults.getSecurityMode());
                statusLabel.setText("Chưa có cấu hình. Vui lòng nhập thông tin mail server.");
            } else {
                MailServerConfig config = loadedAccount.getMailServerConfig();
                usernameField.setText(loadedAccount.getUsername());
                passwordField.setText(loadedAccount.getPassword());
                smtpHostField.setText(config.getSmtpHost());
                smtpPortField.setText(String.valueOf(config.getSmtpPort()));
                imapHostField.setText(config.getImapHost());
                imapPortField.setText(String.valueOf(config.getImapPort()));
                securityModeComboBox.setValue(config.getSecurityMode());
                statusLabel.setText("Đã tải cấu hình hiện có.");
            }
            initialFingerprint = currentFingerprint();
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to load existing mail-server configuration.", ex);
            statusLabel.setText("Không thể tải cấu hình hiện có.");
        } finally {
            loadingConfiguration = false;
        }
    }

    private void bindDirtyTracking() {
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        smtpHostField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        smtpPortField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        imapHostField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        imapPortField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
    }

    private void markConnectionDirty() {
        connectionValidated = false;
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
    }

    private Account buildAccountFromForm() {
        Account account = new Account(
                textOf(usernameField),
                passwordField.getText() == null ? "" : passwordField.getText().replace(" ", "").trim(),
                loadedAccount == null ? LocalDateTime.now() : loadedAccount.getCreatedAt()
        );
        if (loadedAccount != null && loadedAccount.getId() != null) {
            account.setId(loadedAccount.getId());
        }
        account.setMailServerConfig(new MailServerConfig(
                textOf(smtpHostField),
                parsePort(smtpPortField),
                textOf(imapHostField),
                parsePort(imapPortField),
                securityModeComboBox.getValue() == null ? SecurityMode.TLS : securityModeComboBox.getValue()
        ));
        return account;
    }

    private boolean validateForm(Account account) {
        clearValidation();
        boolean valid = true;

        if (!EmailUtil.isValidEmail(account.getUsername())) {
            markInvalid(usernameField, "Địa chỉ email không hợp lệ.");
            valid = false;
        }
        if (account.getPassword().isBlank()) {
            markInvalid(passwordField, "Mật khẩu ứng dụng không được để trống.");
            valid = false;
        }
        if (account.getMailServerConfig().getSmtpHost().isBlank()) {
            markInvalid(smtpHostField, "SMTP host không được để trống.");
            valid = false;
        }
        if (account.getMailServerConfig().getImapHost().isBlank()) {
            markInvalid(imapHostField, "IMAP host không được để trống.");
            valid = false;
        }
        if (!isValidPort(smtpPortField)) {
            markInvalid(smtpPortField, "Cổng SMTP phải là số nguyên từ 1 đến 65535.");
            valid = false;
        }
        if (!isValidPort(imapPortField)) {
            markInvalid(imapPortField, "Cổng IMAP phải là số nguyên từ 1 đến 65535.");
            valid = false;
        }

        if (!valid) {
            statusLabel.setText("Dữ liệu đầu vào không hợp lệ.");
            LOGGER.warn("Mail-server configuration validation failed for username={}.", LogHelper.maskEmail(account.getUsername()));
        }
        return valid;
    }

    private void handleTestCompleted(Account account, Throwable throwable) {
        setTesting(false);
        if (throwable == null) {
            connectionValidated = true;
            saveButton.setDisable(false);
            statusLabel.setText("Kiểm tra kết nối thành công.");
            AlertUtil.showInfo("Success", "Kiểm tra kết nối thành công!");
            LOGGER.info("Mail-server test completed successfully for username={}.", LogHelper.maskEmail(account.getUsername()));
            return;
        }

        connectionValidated = false;
        saveButton.setDisable(true);
        Throwable cause = unwrap(throwable);
        if (cause instanceof AuthenticationFailedException) {
            LOGGER.warn("Mail-server authentication failed for username={}.", LogHelper.maskEmail(account.getUsername()));
            statusLabel.setText("Xác thực thất bại.");
            AlertUtil.showError("Authentication Error", "Xác thực thất bại. Vui lòng kiểm tra lại địa chỉ Email và Mật khẩu ứng dụng.");
        } else if (isConnectionFailure(cause)) {
            LOGGER.warn("Mail-server connection timed out or failed for username={}.", LogHelper.maskEmail(account.getUsername()), cause);
            statusLabel.setText("Không thể kết nối tới máy chủ.");
            AlertUtil.showError("Connection Error", "Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại đường truyền Internet và thông số Port.");
        } else {
            LOGGER.warn("Mail-server test failed for username={}.", LogHelper.maskEmail(account.getUsername()), cause);
            statusLabel.setText("Kiểm tra kết nối thất bại.");
            AlertUtil.showError("Connection Error", "Kiểm tra kết nối thất bại. Vui lòng kiểm tra lại cấu hình mail server.");
        }
    }

    private void handleSaveCompleted(Account savedAccount, Throwable throwable) {
        setTesting(false);
        if (throwable != null) {
            LOGGER.error("Mail-server configuration save failed.", unwrap(throwable));
            statusLabel.setText("Lưu cấu hình thất bại.");
            AlertUtil.showError("Save Error", "Lỗi ghi dữ liệu. Không thể lưu cấu hình xuống ổ đĩa cục bộ. Vui lòng thử lại.");
            return;
        }

        loadedAccount = savedAccount;
        initialFingerprint = currentFingerprint();
        AccountRefreshService.publishAccountsChanged();
        LOGGER.info("Mail-server configuration saved successfully for username={}.", LogHelper.maskEmail(savedAccount.getUsername()));
        AlertUtil.showInfo("Success", "Lưu cấu hình thành công!");
        closingAfterSave = true;
        stage.close();
    }

    private void requestClose() {
        if (stage == null) {
            return;
        }
        if (!hasUnsavedChanges() || confirmDiscardChanges()) {
            stage.close();
        }
    }

    private void handleCloseRequest(WindowEvent event) {
        if (closingAfterSave || !hasUnsavedChanges()) {
            return;
        }
        if (!confirmDiscardChanges()) {
            event.consume();
        }
    }

    private boolean hasUnsavedChanges() {
        return !Objects.equals(initialFingerprint, currentFingerprint());
    }

    private boolean confirmDiscardChanges() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Discard changes");
        alert.setHeaderText(null);
        alert.setContentText("Mọi thay đổi chưa lưu sẽ bị mất. Bạn có chắc chắn muốn thoát?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void applySuggestedPorts(SecurityMode mode) {
        if (mode == SecurityMode.SSL) {
            smtpPortField.setText("465");
            imapPortField.setText("993");
        } else {
            smtpPortField.setText("587");
            if (imapPortField.getText() == null || imapPortField.getText().isBlank()) {
                imapPortField.setText("993");
            }
        }
    }

    private void setTesting(boolean testing) {
        usernameField.setDisable(testing);
        passwordField.setDisable(testing);
        smtpHostField.setDisable(testing);
        smtpPortField.setDisable(testing);
        imapHostField.setDisable(testing);
        imapPortField.setDisable(testing);
        securityModeComboBox.setDisable(testing);
        testConnectionButton.setDisable(testing);
        cancelButton.setDisable(testing);
        saveButton.setDisable(testing || !connectionValidated);
        progressIndicator.setVisible(testing);
    }

    private boolean isConnectionFailure(Throwable cause) {
        while (cause != null) {
            if (cause instanceof ConnectException || cause instanceof SocketTimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while ((current instanceof CompletionException || current instanceof RuntimeException) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String currentFingerprint() {
        return String.join("|",
                textOf(usernameField),
                passwordField.getText() == null ? "" : passwordField.getText(),
                textOf(smtpHostField),
                textOf(smtpPortField),
                textOf(imapHostField),
                textOf(imapPortField),
                String.valueOf(securityModeComboBox.getValue())
        );
    }

    private void clearValidation() {
        clearInvalid(usernameField);
        clearInvalid(passwordField);
        clearInvalid(smtpHostField);
        clearInvalid(smtpPortField);
        clearInvalid(imapHostField);
        clearInvalid(imapPortField);
    }

    private void markInvalid(TextField field, String message) {
        field.setStyle(INVALID_STYLE);
        field.setTooltip(new Tooltip(message));
    }

    private void clearInvalid(TextField field) {
        field.setStyle(null);
        field.setTooltip(null);
    }

    private boolean isValidPort(TextField field) {
        int port = parsePort(field);
        return port >= 1 && port <= 65535;
    }

    private int parsePort(TextField field) {
        try {
            return Integer.parseInt(textOf(field));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String textOf(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
