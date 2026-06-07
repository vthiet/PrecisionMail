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
import nlu.fit.soft.gr5.precisionMail.model.ConnectionTestProgress;
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

    /**
     * Xử lý sự kiện kiểm tra kết nối trong UC-01.
     *
     * <p>Commit UC-01 #17-#18 - Anh Han: chạy kiểm tra SMTP/IMAP ở background
     * thread, nhận {@link ConnectionTestResult} an toàn và cập nhật tiến trình
     * từng bước cho UI.</p>
     *
     * @param actionEvent sự kiện click từ JavaFX
     */
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
        statusLabel.setText("Đang kiểm tra SMTP...");

        CompletableFuture
                .supplyAsync(
                        () -> emailService.validateConnection(account, this::updateConnectionTestProgress),
                        AppExecutors.io()
                )
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

    /**
     * Đánh dấu cấu hình cần kiểm tra lại sau khi người dùng thay đổi dữ liệu.
     *
     * <p>Commit UC-01 #15 - Anh Han: không cho lưu cấu hình SMTP/IMAP đã thay
     * đổi nếu chưa kiểm tra kết nối lại.</p>
     */
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

    /**
     * Xử lý khi người dùng tự sửa host/port SMTP hoặc IMAP.
     *
     * <p>Commit UC-01 #9 - Anh Han: chuyển provider về Custom để giữ đúng
     * hành vi cấu hình thủ công.</p>
     */
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

    /**
     * Tạo model tài khoản từ dữ liệu trên form cấu hình UC-01.
     *
     * <p>Commit UC-01 #10-#12 - Anh Han: lấy display name, primary flag và
     * security mode riêng cho SMTP/IMAP.</p>
     *
     * @return tài khoản tạm dùng để validate, test connection hoặc lưu
     */
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

    /**
     * Xử lý kết quả cuối cùng của quá trình kiểm tra SMTP/IMAP.
     *
     * <p>Commit UC-01 #17 - Anh Han: thành công thì mở khóa lưu cấu hình;
     * thất bại thì khóa lưu, yêu cầu kiểm tra lại và hiển thị lỗi theo loại/bước.</p>
     *
     * @param account tài khoản vừa được kiểm tra
     * @param result kết quả kiểm tra kết nối
     * @param throwable lỗi ngoài dự kiến nếu background task thất bại
     */
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
        ConnectionTestResult.Step failureStep = result == null
                ? ConnectionTestResult.Step.UNKNOWN
                : result.step();
        showConnectionTestFailure(account, failureType, failureStep, result == null ? null : result.cause());
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

    /**
     * Áp dụng cấu hình SMTP/IMAP theo provider được chọn.
     *
     * <p>Commit UC-01 #8 - Anh Han: tự điền host, port và security mode cho
     * Gmail, Outlook hoặc Yahoo.</p>
     *
     * @param preset provider được chọn trên ComboBox
     */
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

    /**
     * Hiển thị hoặc ẩn trạng thái cần kiểm tra lại.
     *
     * @param required true nếu cấu hình hiện tại cần test lại trước khi lưu
     */
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

    /**
     * Xử lý lỗi ngoài dự kiến phát sinh từ background task kiểm tra kết nối.
     *
     * @param account tài khoản đang được kiểm tra
     * @param throwable exception gốc từ {@link CompletableFuture}
     */
    private void handleUnexpectedTestFailure(Account account, Throwable throwable) {
        Throwable cause = unwrap(throwable);
        connectionValidated = false;
        setRetestRequired(true);
        saveButton.setDisable(true);
        showConnectionTestFailure(account, ConnectionTestResult.Type.UNKNOWN_FAILED, ConnectionTestResult.Step.UNKNOWN, cause);
    }

    /**
     * Hiển thị lỗi kiểm tra kết nối theo loại lỗi và bước lỗi.
     *
     * <p>Commit UC-01 #17 - Anh Han: AUTH_FAILED và TIMEOUT có thể xảy ra ở
     * SMTP hoặc IMAP, nên cần dùng thêm step để báo đúng vị trí lỗi.</p>
     *
     * @param account tài khoản đang kiểm tra
     * @param type loại lỗi kiểm tra kết nối
     * @param step bước xảy ra lỗi
     * @param cause exception gốc phục vụ logging/debug
     */
    private void showConnectionTestFailure(
            Account account,
            ConnectionTestResult.Type type,
            ConnectionTestResult.Step step,
            Throwable cause
    ) {
        LOGGER.warn(
                "Mail-server test failed for username={}. type={}, step={}.",
                LogHelper.maskEmail(account.getUsername()),
                type,
                step,
                cause
        );

        if (type == ConnectionTestResult.Type.AUTH_FAILED) {
            statusLabel.setText(connectionFailureStatus(step, "xác thực thất bại"));
            AlertUtil.showError(
                    "Authentication Error",
                    connectionFailureMessage(step, "Xác thực thất bại.")
                            + " Vui lòng kiểm tra lại địa chỉ Email và Mật khẩu ứng dụng."
            );
            return;
        }

        if (type == ConnectionTestResult.Type.TIMEOUT) {
            statusLabel.setText(connectionFailureStatus(step, "không thể kết nối tới máy chủ"));
            AlertUtil.showError(
                    "Connection Error",
                    connectionFailureMessage(step, "Không thể kết nối tới máy chủ.")
                            + " Vui lòng kiểm tra lại đường truyền Internet, host và port."
            );
            return;
        }

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

    /**
     * Cập nhật trạng thái UI theo tiến trình kiểm tra SMTP/IMAP.
     *
     * <p>Commit UC-01 #18 - Anh Han: đưa cập nhật label về JavaFX Application
     * Thread để UI báo rõ đang test SMTP hay IMAP.</p>
     *
     * @param progress trạng thái tiến trình kiểm tra hiện tại
     */
    private void updateConnectionTestProgress(ConnectionTestProgress progress) {
        Platform.runLater(() -> {
            switch (progress) {
                case SMTP_TESTING -> statusLabel.setText("Đang kiểm tra SMTP...");
                case SMTP_SUCCEEDED -> statusLabel.setText("SMTP thành công. Đang chuẩn bị kiểm tra IMAP...");
                case IMAP_TESTING -> statusLabel.setText("Đang kiểm tra IMAP...");
                case IMAP_SUCCEEDED -> statusLabel.setText("IMAP thành công. Đang hoàn tất kiểm tra...");
            }
        });
    }

    /**
     * Tạo nội dung status ngắn gọn cho lỗi kiểm tra kết nối.
     *
     * @param step bước xảy ra lỗi
     * @param detail mô tả lỗi ngắn
     * @return nội dung status hiển thị trên dialog
     */
    private String connectionFailureStatus(ConnectionTestResult.Step step, String detail) {
        return switch (step) {
            case SMTP -> "SMTP " + detail + ".";
            case IMAP -> "IMAP " + detail + ".";
            default -> "Kiểm tra kết nối " + detail + ".";
        };
    }

    /**
     * Tạo thông điệp lỗi chi tiết cho popup kiểm tra kết nối.
     *
     * @param step bước xảy ra lỗi
     * @param fallbackMessage thông điệp mặc định nếu không xác định được bước lỗi
     * @return thông điệp lỗi phù hợp với SMTP/IMAP
     */
    private String connectionFailureMessage(ConnectionTestResult.Step step, String fallbackMessage) {
        return switch (step) {
            case SMTP -> "Lỗi ở bước kiểm tra SMTP.";
            case IMAP -> "Lỗi ở bước kiểm tra IMAP.";
            default -> fallbackMessage;
        };
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

    /**
     * Áp dụng trạng thái lỗi giải mã App Password lên form cấu hình.
     *
     * <p>Commit UC-01 #16 - Anh Han: nếu password đã lưu không giải mã được,
     * form yêu cầu nhập lại App Password và kiểm tra kết nối trước khi lưu.</p>
     *
     * @param account tài khoản đang được nạp lên form
     */
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
