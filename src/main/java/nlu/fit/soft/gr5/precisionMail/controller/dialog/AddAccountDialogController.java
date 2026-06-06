package nlu.fit.soft.gr5.precisionMail.controller.dialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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
import nlu.fit.soft.gr5.precisionMail.model.ConnectionTestResult;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.model.MailProviderPreset;
import nlu.fit.soft.gr5.precisionMail.model.SecurityMode;
import nlu.fit.soft.gr5.precisionMail.service.AccountRefreshService;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import nlu.fit.soft.gr5.precisionMail.util.MailServerConfigValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AddAccountDialogController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddAccountDialogController.class);
    private static final String INVALID_STYLE = "-fx-border-color: #dc2626; -fx-border-width: 1.2;";

    @FXML
    public ComboBox<MailProviderPreset> providerComboBox;
    @FXML
    public TextField displayNameField;
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
    public ComboBox<SecurityMode> smtpSecurityModeComboBox;
    @FXML
    public ComboBox<SecurityMode> imapSecurityModeComboBox;
    @FXML
    public Button testConnectionButton;
    @FXML
    public Button saveButton;
    @FXML
    public Button cancelButton;
    @FXML
    public CheckBox primaryCheckBox;
    @FXML
    public ProgressIndicator progressIndicator;
    @FXML
    public Label retestRequiredLabel;
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
    private boolean applyingProviderPreset;
    private boolean editingExistingAccount;
    private boolean retestRequired;
    private boolean passwordDecryptionFailed;

    @FXML
    public void initialize() {
        providerComboBox.getItems().setAll(MailProviderPreset.values());
        providerComboBox.setValue(MailProviderPreset.GMAIL);
        providerComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (loadingConfiguration) {
                return;
            }
            applyProviderPreset(newValue);
        });

        smtpSecurityModeComboBox.getItems().setAll(SecurityMode.values());
        smtpSecurityModeComboBox.setValue(SecurityMode.TLS);
        smtpSecurityModeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (loadingConfiguration || applyingProviderPreset) {
                return;
            }
            applySuggestedSmtpPort(newValue);
            markProviderAsCustomForManualEdit();
            markConnectionDirty();
        });

        imapSecurityModeComboBox.getItems().setAll(SecurityMode.values());
        imapSecurityModeComboBox.setValue(SecurityMode.SSL);
        imapSecurityModeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (loadingConfiguration || applyingProviderPreset) {
                return;
            }
            applySuggestedImapPort(newValue);
            markProviderAsCustomForManualEdit();
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

    public void prepareNewAccount() {
        loadingConfiguration = true;
        try {
            loadedAccount = null;
            editingExistingAccount = false;
            MailServerConfig defaults = new MailServerConfig();
            displayNameField.clear();
            usernameField.clear();
            passwordField.clear();
            primaryCheckBox.setSelected(true);
            smtpHostField.setText(defaults.getSmtpHost());
            smtpPortField.setText(String.valueOf(defaults.getSmtpPort()));
            imapHostField.setText(defaults.getImapHost());
            imapPortField.setText(String.valueOf(defaults.getImapPort()));
            smtpSecurityModeComboBox.setValue(defaults.getSmtpSecurityMode());
            imapSecurityModeComboBox.setValue(defaults.getImapSecurityMode());
            providerComboBox.setValue(MailProviderPreset.inferFrom(defaults));
            statusLabel.setText("Nhập thông tin tài khoản mail mới.");
            connectionValidated = false;
            passwordDecryptionFailed = false;
            setRetestRequired(false);
            initialFingerprint = currentFingerprint();
            setTesting(false);
        } finally {
            loadingConfiguration = false;
        }
    }

    public void prepareEditAccount(Account account) {
        if (account == null) {
            prepareNewAccount();
            return;
        }

        loadingConfiguration = true;
        try {
            loadedAccount = account;
            editingExistingAccount = true;
            populateAccountForm(account);
            applyPasswordDecryptionState(account);
            connectionValidated = false;
            initialFingerprint = currentFingerprint();
            setTesting(false);
        } finally {
            loadingConfiguration = false;
        }
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
        setRetestRequired(false);
        statusLabel.setText("Đang kiểm tra kết nối...");

        CompletableFuture
                .supplyAsync(() -> emailService.validateConnection(account), AppExecutors.io())
                .whenComplete((result, throwable) -> Platform.runLater(() -> handleTestCompleted(account, result, throwable)));
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
                primaryCheckBox.setSelected(true);
                smtpHostField.setText(defaults.getSmtpHost());
                smtpPortField.setText(String.valueOf(defaults.getSmtpPort()));
                imapHostField.setText(defaults.getImapHost());
                imapPortField.setText(String.valueOf(defaults.getImapPort()));
                smtpSecurityModeComboBox.setValue(defaults.getSmtpSecurityMode());
                imapSecurityModeComboBox.setValue(defaults.getImapSecurityMode());
                providerComboBox.setValue(MailProviderPreset.inferFrom(defaults));
                statusLabel.setText("Chưa có cấu hình. Vui lòng nhập thông tin mail server.");
            } else {
                editingExistingAccount = true;
                populateAccountForm(loadedAccount);
                applyPasswordDecryptionState(loadedAccount);
            }
            initialFingerprint = currentFingerprint();
            if (loadedAccount == null || !loadedAccount.isPasswordDecryptionFailed()) {
                setRetestRequired(false);
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to load existing mail-server configuration.", ex);
            statusLabel.setText("Không thể tải cấu hình hiện có.");
        } finally {
            loadingConfiguration = false;
        }
    }

    private void bindDirtyTracking() {
        displayNameField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        primaryCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> markConnectionDirty());
        smtpHostField.textProperty().addListener((observable, oldValue, newValue) -> handleManualServerConfigurationChange());
        smtpPortField.textProperty().addListener((observable, oldValue, newValue) -> handleManualServerConfigurationChange());
        imapHostField.textProperty().addListener((observable, oldValue, newValue) -> handleManualServerConfigurationChange());
        imapPortField.textProperty().addListener((observable, oldValue, newValue) -> handleManualServerConfigurationChange());
    }

    private void markConnectionDirty() {
        if (loadingConfiguration) {
            return;
        }

        passwordDecryptionFailed = false;
        boolean wasValidated = connectionValidated;
        connectionValidated = false;
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
        setRetestRequired(true);
        if (statusLabel != null) {
            statusLabel.setText(wasValidated
                    ? "Cấu hình đã thay đổi. Vui lòng kiểm tra kết nối lại trước khi lưu."
                    : "Cần kiểm tra kết nối trước khi lưu cấu hình.");
        }
    }

    private void handleManualServerConfigurationChange() {
        markProviderAsCustomForManualEdit();
        markConnectionDirty();
    }

    private void markProviderAsCustomForManualEdit() {
        if (loadingConfiguration || applyingProviderPreset || providerComboBox == null) {
            return;
        }
        if (providerComboBox.getValue() != MailProviderPreset.CUSTOM) {
            providerComboBox.setValue(MailProviderPreset.CUSTOM);
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
        account.setDisplayName(displayNameFromForm(account.getUsername()));
        account.setPrimary(primaryCheckBox.isSelected());
        account.setMailServerConfig(new MailServerConfig(
                textOf(smtpHostField),
                MailServerConfigValidator.parsePort(textOf(smtpPortField)),
                textOf(imapHostField),
                MailServerConfigValidator.parsePort(textOf(imapPortField)),
                smtpSecurityModeComboBox.getValue() == null ? SecurityMode.TLS : smtpSecurityModeComboBox.getValue(),
                imapSecurityModeComboBox.getValue() == null ? SecurityMode.SSL : imapSecurityModeComboBox.getValue()
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
        if (passwordDecryptionFailed) {
            markInvalid(passwordField, "Không thể giải mã App Password đã lưu. Vui lòng nhập lại App Password.");
            valid = false;
        }
        if (account.getDisplayName().isBlank()) {
            markInvalid(displayNameField, "Display name is required.");
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
        if (!MailServerConfigValidator.isValidPort(textOf(smtpPortField))) {
            markInvalid(smtpPortField, "Cổng SMTP phải là số nguyên từ 1 đến 65535.");
            valid = false;
        }
        if (!MailServerConfigValidator.isValidPort(textOf(imapPortField))) {
            markInvalid(imapPortField, "Cổng IMAP phải là số nguyên từ 1 đến 65535.");
            valid = false;
        }

        if (!valid) {
            statusLabel.setText("Dữ liệu đầu vào không hợp lệ.");
            LOGGER.warn("Mail-server configuration validation failed for username={}.", LogHelper.maskEmail(account.getUsername()));
        }
        return valid;
    }

    private void handleTestCompleted(Account account, ConnectionTestResult result, Throwable throwable) {
        setTesting(false);
        if (throwable != null) {
            handleUnexpectedTestFailure(account, throwable);
            return;
        }

        if (result != null && result.isSuccess()) {
            connectionValidated = true;
            setRetestRequired(false);
            saveButton.setDisable(false);
            statusLabel.setText("Kiểm tra kết nối thành công.");
            AlertUtil.showInfo("Success", "Kiểm tra kết nối thành công!");
            LOGGER.info("Mail-server test completed successfully for username={}.", LogHelper.maskEmail(account.getUsername()));
            return;
        }

        connectionValidated = false;
        setRetestRequired(true);
        saveButton.setDisable(true);
        ConnectionTestResult.Type failureType = result == null
                ? ConnectionTestResult.Type.UNKNOWN_FAILED
                : result.type();
        showConnectionTestFailure(account, failureType, result == null ? null : result.cause());
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
        setRetestRequired(false);
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

    private void applySuggestedSmtpPort(SecurityMode mode) {
        if (mode == SecurityMode.SSL) {
            smtpPortField.setText("465");
        } else {
            smtpPortField.setText("587");
        }
    }

    private void applySuggestedImapPort(SecurityMode mode) {
        if (mode == SecurityMode.SSL) {
            imapPortField.setText("993");
        } else {
            imapPortField.setText("143");
        }
    }

    private void applyProviderPreset(MailProviderPreset preset) {
        if (preset == null || !preset.hasConfig()) {
            markConnectionDirty();
            return;
        }

        applyingProviderPreset = true;
        try {
            MailServerConfig config = preset.getConfig();
            smtpHostField.setText(config.getSmtpHost());
            smtpPortField.setText(String.valueOf(config.getSmtpPort()));
            imapHostField.setText(config.getImapHost());
            imapPortField.setText(String.valueOf(config.getImapPort()));
            smtpSecurityModeComboBox.setValue(config.getSmtpSecurityMode());
            imapSecurityModeComboBox.setValue(config.getImapSecurityMode());
        } finally {
            applyingProviderPreset = false;
        }
        markConnectionDirty();
    }

    private void setTesting(boolean testing) {
        providerComboBox.setDisable(testing);
        displayNameField.setDisable(testing);
        usernameField.setDisable(testing || editingExistingAccount);
        passwordField.setDisable(testing);
        smtpHostField.setDisable(testing);
        smtpPortField.setDisable(testing);
        imapHostField.setDisable(testing);
        imapPortField.setDisable(testing);
        smtpSecurityModeComboBox.setDisable(testing);
        imapSecurityModeComboBox.setDisable(testing);
        testConnectionButton.setDisable(testing);
        cancelButton.setDisable(testing);
        saveButton.setDisable(testing || !connectionValidated);
        primaryCheckBox.setDisable(testing);
        progressIndicator.setVisible(testing);
    }

    private void setRetestRequired(boolean required) {
        if (retestRequired == required && retestRequiredLabel != null) {
            return;
        }
        retestRequired = required;
        if (retestRequiredLabel != null) {
            retestRequiredLabel.setVisible(required);
            retestRequiredLabel.setManaged(required);
        }
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while ((current instanceof CompletionException || current instanceof RuntimeException) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private void handleUnexpectedTestFailure(Account account, Throwable throwable) {
        Throwable cause = unwrap(throwable);
        connectionValidated = false;
        setRetestRequired(true);
        saveButton.setDisable(true);
        showConnectionTestFailure(account, ConnectionTestResult.Type.UNKNOWN_FAILED, cause);
    }

    private void showConnectionTestFailure(
            Account account,
            ConnectionTestResult.Type type,
            Throwable cause
    ) {
        LOGGER.warn(
                "Mail-server test failed for username={}. type={}.",
                LogHelper.maskEmail(account.getUsername()),
                type,
                cause
        );

        switch (type) {
            case AUTH_FAILED -> {
                statusLabel.setText("Xác thực thất bại.");
                AlertUtil.showError(
                        "Authentication Error",
                        "Xác thực thất bại. Vui lòng kiểm tra lại địa chỉ Email và Mật khẩu ứng dụng."
                );
            }
            case TIMEOUT -> {
                statusLabel.setText("Không thể kết nối tới máy chủ.");
                AlertUtil.showError(
                        "Connection Error",
                        "Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại đường truyền Internet, host và port."
                );
            }
            case SMTP_FAILED -> {
                statusLabel.setText("Kiểm tra SMTP thất bại.");
                AlertUtil.showError(
                        "SMTP Error",
                        "Không thể kết nối hoặc xác thực SMTP. Vui lòng kiểm tra SMTP host, port và security mode."
                );
            }
            case IMAP_FAILED -> {
                statusLabel.setText("Kiểm tra IMAP thất bại.");
                AlertUtil.showError(
                        "IMAP Error",
                        "Không thể kết nối hoặc xác thực IMAP. Vui lòng kiểm tra IMAP host, port và security mode."
                );
            }
            default -> {
                statusLabel.setText("Kiểm tra kết nối thất bại.");
                AlertUtil.showError(
                        "Connection Error",
                        "Kiểm tra kết nối thất bại. Vui lòng kiểm tra lại cấu hình mail server."
                );
            }
        }
    }

    private String currentFingerprint() {
        return String.join("|",
                textOf(displayNameField),
                textOf(usernameField),
                passwordField.getText() == null ? "" : passwordField.getText(),
                String.valueOf(primaryCheckBox.isSelected()),
                textOf(smtpHostField),
                textOf(smtpPortField),
                textOf(imapHostField),
                textOf(imapPortField),
                String.valueOf(providerComboBox.getValue()),
                String.valueOf(smtpSecurityModeComboBox.getValue()),
                String.valueOf(imapSecurityModeComboBox.getValue())
        );
    }

    private void clearValidation() {
        clearInvalid(displayNameField);
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

    private String textOf(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String displayNameFromForm(String username) {
        String displayName = textOf(displayNameField);
        return displayName.isBlank() ? username : displayName;
    }

    private void populateAccountForm(Account account) {
        MailServerConfig config = account.getMailServerConfig();
        displayNameField.setText(account.getDisplayName());
        usernameField.setText(account.getUsername());
        passwordField.setText(account.isPasswordDecryptionFailed() ? "" : account.getPassword());
        primaryCheckBox.setSelected(account.isPrimary());
        smtpHostField.setText(config.getSmtpHost());
        smtpPortField.setText(String.valueOf(config.getSmtpPort()));
        imapHostField.setText(config.getImapHost());
        imapPortField.setText(String.valueOf(config.getImapPort()));
        smtpSecurityModeComboBox.setValue(config.getSmtpSecurityMode());
        imapSecurityModeComboBox.setValue(config.getImapSecurityMode());
        providerComboBox.setValue(MailProviderPreset.inferFrom(config));
    }

    private void applyPasswordDecryptionState(Account account) {
        passwordDecryptionFailed = account.isPasswordDecryptionFailed();
        if (passwordDecryptionFailed) {
            connectionValidated = false;
            saveButton.setDisable(true);
            markInvalid(passwordField, "Không thể giải mã App Password đã lưu. Vui lòng nhập lại App Password.");
            statusLabel.setText("Không thể giải mã App Password đã lưu. Vui lòng nhập lại mật khẩu và kiểm tra kết nối.");
            setRetestRequired(true);
            return;
        }
        setRetestRequired(false);
        statusLabel.setText(editingExistingAccount ? "Đã tải cấu hình hiện có." : "Nhập thông tin tài khoản mail mới.");
    }
}
