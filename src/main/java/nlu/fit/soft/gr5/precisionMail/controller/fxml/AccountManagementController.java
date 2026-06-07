package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.controller.dialog.AddAccountDialogController;
import nlu.fit.soft.gr5.precisionMail.model.Account;
import nlu.fit.soft.gr5.precisionMail.model.MailProviderPreset;
import nlu.fit.soft.gr5.precisionMail.model.MailServerConfig;
import nlu.fit.soft.gr5.precisionMail.service.AccountRefreshService;
import nlu.fit.soft.gr5.precisionMail.service.AccountService;
import nlu.fit.soft.gr5.precisionMail.service.impl.AccountServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller quản lý danh sách tài khoản email của UC-01.
 *
 * <p>Commit UC-01 #13-#14 - Anh Han: bổ sung màn hình danh sách tài khoản,
 * sửa, xóa có xác nhận và đặt tài khoản mặc định.</p>
 *
 * @author Anh Han
 */
public class AccountManagementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManagementController.class);
    private static final ButtonType DELETE_BUTTON = new ButtonType("Xóa", ButtonType.OK.getButtonData());

    @FXML
    public TableView<Account> accountTable;
    @FXML
    public TableColumn<Account, String> displayNameColumn;
    @FXML
    public TableColumn<Account, String> emailColumn;
    @FXML
    public TableColumn<Account, String> providerColumn;
    @FXML
    public TableColumn<Account, String> smtpColumn;
    @FXML
    public TableColumn<Account, String> imapColumn;
    @FXML
    public TableColumn<Account, String> primaryColumn;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button makePrimaryButton;
    @FXML
    public Label statusLabel;

    private final AccountService accountService = new AccountServiceImpl();
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        displayNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDisplayName()));
        emailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getUsername()));
        providerColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(providerLabel(data.getValue())));
        smtpColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(smtpLabel(data.getValue())));
        imapColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(imapLabel(data.getValue())));
        primaryColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().isPrimary() ? "Mặc định" : ""));

        accountTable.setItems(accounts);
        accountTable.setPlaceholder(new Label("Chưa có tài khoản mail nào."));
        accountTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> updateActionState(selected));
        accountTable.setRowFactory(table -> {
            TableRow<Account> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openEditDialog(row.getItem());
                }
            });
            return row;
        });

        updateActionState(null);
        loadAccounts();
    }

    @FXML
    public void handleRefresh() {
        loadAccounts();
    }

    @FXML
    public void handleAddAccount() {
        openAccountDialog(null);
    }

    @FXML
    public void handleEditAccount() {
        Account selected = selectedAccount();
        if (selected != null) {
            openEditDialog(selected);
        }
    }

    @FXML
    /**
     * Xóa tài khoản đang chọn sau khi người dùng xác nhận.
     *
     * <p>Commit UC-01 #14 - Anh Han: nối UI với service/DAO xóa tài khoản và
     * cập nhật lại danh sách sau thao tác.</p>
     */
    public void handleDeleteAccount() {
        Account selected = selectedAccount();
        if (selected == null) {
            statusLabel.setText("Chọn một tài khoản trước khi xóa.");
            return;
        }

        if (!confirmDelete(selected)) {
            statusLabel.setText("Đã hủy xóa tài khoản.");
            return;
        }

        try {
            accountService.deleteByEmailAddress(selected.getUsername());
            AccountRefreshService.publishAccountsChanged();
            LOGGER.info("Account deleted from management screen. username={}.", LogHelper.maskEmail(selected.getUsername()));
            loadAccounts();
            statusLabel.setText("Đã xóa tài khoản " + selected.getDisplayName() + ".");
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to delete account from management screen. username={}.", LogHelper.maskEmail(selected.getUsername()), ex);
            AlertUtil.showError("Xóa tài khoản thất bại", "Không thể xóa tài khoản đã chọn. Vui lòng thử lại.");
        }
    }

    @FXML
    /**
     * Đặt tài khoản đang chọn làm tài khoản mặc định.
     *
     * <p>Commit UC-01 #12-#13 - Anh Han: hỗ trợ nhiều tài khoản và chọn tài
     * khoản chính dùng cho luồng gửi/nhận mail.</p>
     */
    public void handleMakePrimary() {
        Account selected = selectedAccount();
        if (selected == null || selected.isPrimary()) {
            return;
        }

        try {
            selected.setPrimary(true);
            accountService.update(selected);
            AccountRefreshService.publishAccountsChanged();
            LOGGER.info("Primary account changed from management screen. username={}.", LogHelper.maskEmail(selected.getUsername()));
            loadAccounts();
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to set primary account. username={}.", LogHelper.maskEmail(selected.getUsername()), ex);
            AlertUtil.showError("Đặt mặc định thất bại", "Không thể đặt tài khoản đã chọn làm mặc định. Vui lòng thử lại.");
        }
    }

    /**
     * Tải lại danh sách tài khoản đã lưu và cập nhật bảng quản lý.
     */
    private void loadAccounts() {
        try {
            accounts.setAll(accountService.findAll());
            if (accounts.isEmpty()) {
                statusLabel.setText("Chưa có tài khoản mail nào.");
            } else {
                statusLabel.setText("Đã tải " + accounts.size() + " tài khoản.");
            }
            updateActionState(selectedAccount());
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to load accounts for management screen.", ex);
            statusLabel.setText("Không thể tải danh sách tài khoản.");
            AlertUtil.showError("Tải tài khoản thất bại", "Không thể đọc danh sách tài khoản đã cấu hình.");
        }
    }

    private void openEditDialog(Account account) {
        if (account != null) {
            openAccountDialog(account);
        }
    }

    private void openAccountDialog(Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/nlu/fit/soft/gr5/precisionMail/view/dialog/add-account-dialog.fxml")
            );
            Parent root = loader.load();
            AddAccountDialogController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            if (account == null) {
                controller.prepareNewAccount();
                stage.setTitle("Thêm tài khoản");
            } else {
                controller.prepareEditAccount(account);
                stage.setTitle("Sửa tài khoản");
            }
            stage.setScene(new Scene(root, 540, 500));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(accountTable.getScene().getWindow());
            stage.showAndWait();
            loadAccounts();
        } catch (IOException e) {
            LOGGER.error("Failed to open account dialog from management screen.", e);
            AlertUtil.showError("Mở cấu hình thất bại", "Không thể mở hộp thoại cấu hình tài khoản.");
        }
    }

    /**
     * Hiển thị hộp thoại xác nhận trước khi xóa tài khoản.
     *
     * @param account tài khoản chuẩn bị xóa
     * @return true nếu người dùng xác nhận xóa
     */
    private boolean confirmDelete(Account account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa tài khoản");
        alert.setHeaderText("Xóa tài khoản " + account.getDisplayName() + "?");
        alert.setContentText(deleteConfirmationMessage(account));
        alert.getButtonTypes().setAll(DELETE_BUTTON, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.CANCEL) == DELETE_BUTTON;
    }

    /**
     * Tạo nội dung cảnh báo xóa tài khoản theo trạng thái hiện tại.
     *
     * @param account tài khoản chuẩn bị xóa
     * @return nội dung xác nhận hiển thị cho người dùng
     */
    private String deleteConfirmationMessage(Account account) {
        StringBuilder message = new StringBuilder()
                .append("Email: ")
                .append(account.getUsername())
                .append(System.lineSeparator())
                .append("Cấu hình SMTP/IMAP đã lưu cho tài khoản này sẽ bị xóa.");

        if (account.isPrimary() && accounts.size() > 1) {
            message.append(System.lineSeparator())
                    .append("Tài khoản này đang là mặc định. Sau khi xóa, hệ thống sẽ tự chọn tài khoản còn lại làm mặc định.");
        }

        if (accounts.size() == 1) {
            message.append(System.lineSeparator())
                    .append("Đây là tài khoản cuối cùng. Sau khi xóa, bạn cần thêm tài khoản mới trước khi gửi mail.");
        }

        return message.toString();
    }

    private void updateActionState(Account selected) {
        boolean hasSelection = selected != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        makePrimaryButton.setDisable(!hasSelection || selected.isPrimary());
    }

    private Account selectedAccount() {
        return accountTable.getSelectionModel().getSelectedItem();
    }

    private String providerLabel(Account account) {
        MailProviderPreset preset = MailProviderPreset.inferFrom(account.getMailServerConfig());
        return preset == MailProviderPreset.CUSTOM ? "Custom" : preset.name();
    }

    private String smtpLabel(Account account) {
        MailServerConfig config = account.getMailServerConfig();
        return config.getSmtpHost() + ":" + config.getSmtpPort() + " " + config.getSmtpSecurityMode();
    }

    private String imapLabel(Account account) {
        MailServerConfig config = account.getMailServerConfig();
        return config.getImapHost() + ":" + config.getImapPort() + " " + config.getImapSecurityMode();
    }
}
