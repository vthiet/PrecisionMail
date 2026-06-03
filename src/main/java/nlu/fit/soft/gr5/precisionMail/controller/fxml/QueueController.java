package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.model.EmailStatus;
import nlu.fit.soft.gr5.precisionMail.model.ScheduledEmail;
import nlu.fit.soft.gr5.precisionMail.service.QueueSearchCriteria;
import nlu.fit.soft.gr5.precisionMail.service.QueueService;
import nlu.fit.soft.gr5.precisionMail.service.impl.QueueServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import nlu.fit.soft.gr5.precisionMail.util.EmailUtil;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueueController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueController.class);
    private static final long MINIMUM_LEAD_TIME_SECONDS = 60;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public TableView<ScheduledEmail> queueTable;
    @FXML
    public TableColumn<ScheduledEmail, String> idColumn;
    @FXML
    public TableColumn<ScheduledEmail, String> senderColumn;
    @FXML
    public TableColumn<ScheduledEmail, String> recipientsColumn;
    @FXML
    public TableColumn<ScheduledEmail, String> subjectColumn;
    @FXML
    public TableColumn<ScheduledEmail, String> scheduledAtColumn;
    @FXML
    public TableColumn<ScheduledEmail, String> statusColumn;
    @FXML
    public Label statusLabel;
    @FXML
    public Label detailLabel;

    private final QueueService queueService = new QueueServiceImpl();
    private final ObservableList<ScheduledEmail> queuedEmails = FXCollections.observableArrayList();

    @FXML
    private TextField txtKeyword;

    @FXML
    private ComboBox<EmailStatus> cbStatus;

    @FXML
    private ComboBox<String> cbSortBy;

    @FXML
    private ComboBox<String> cbSortDirection;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().id)));
        senderColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(LogHelper.maskEmail(data.getValue().email.from)));
        recipientsColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(LogHelper.recipientCount(data.getValue().email))));
        subjectColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(nullToBlank(data.getValue().email.subject)));
        scheduledAtColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatDateTime(data.getValue().scheduledAt)));
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().status.name()));

        queueTable.setItems(queuedEmails);
        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) ->
                detailLabel.setText(selected == null ? "" : detailFor(selected)));

        cbStatus.getItems().addAll(
                EmailStatus.values()
        );

        cbSortBy.getItems().addAll(
                "ID",
                "Subject",
                "Scheduled Time",
                "Status"
        );

        cbSortDirection.getItems().addAll(
                "ASC",
                "DESC"
        );

        cbSortBy.setValue("Scheduled Time");
        cbSortDirection.setValue("DESC");

        refreshQueue();
    }

    @FXML
    public void handleRefresh() {
        refreshQueue();
    }

    @FXML
    public void handleViewDetail() {
        ScheduledEmail selected = selectedEmail();
        if (selected == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết email chờ gửi");
        alert.setHeaderText(null);
        alert.setContentText(detailFor(selected));
        alert.showAndWait();
    }

    @FXML
    public void handleCancelTask() {
        ScheduledEmail selected = selectedEmail();
        if (selected == null || !ensureCanModify(selected)) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy lịch gửi");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn hủy lịch gửi cho email này?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        setBusy("Đang hủy lịch gửi...");
        AppExecutors.io().execute(() -> {
            try {
                queueService.markCancelled(selected.id);
                LOGGER.info(
                        "User cancelled task ID [{}]. Previous target: [{}]. Status: SUCCESS",
                        selected.id,
                        formatDateTime(selected.scheduledAt)
                );
                Platform.runLater(() -> {
                    AlertUtil.showInfo("Thành công", "Đã hủy lịch gửi email thành công!");
                    refreshQueue();
                });
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Queue task cancellation failed. taskId={}.", selected.id, e);
                Platform.runLater(() -> {
                    statusLabel.setText("Hủy lịch gửi thất bại.");
                    AlertUtil.showError(
                            "Hệ thống bận",
                            "Hệ thống bận. Không thể cập nhật trạng thái hàng đợi, vui lòng thử lại sau ít giây."
                    );
                    refreshQueue();
                });
            }
        });
    }

    @FXML
    public void handleEditTask() {
        ScheduledEmail selected = selectedEmail();
        if (selected == null || !ensureCanModify(selected)) {
            return;
        }

        EditForm form = showEditDialog(selected);
        if (form == null) {
            return;
        }

        if (!validateEditForm(form)) {
            return;
        }

        Email updatedEmail = new Email(
                selected.email.from,
                EmailUtil.emailFeature(form.toField.getText()),
                EmailUtil.emailFeature(form.ccField.getText()),
                EmailUtil.emailFeature(form.bccField.getText()),
                form.subjectField.getText(),
                form.bodyArea.getText(),
                attachmentPaths(form.attachmentArea.getText()),
                null
        );
        LocalDateTime newScheduledAt = LocalDateTime.of(
                form.datePicker.getValue(),
                LocalTime.of(form.hourBox.getValue(), form.minuteBox.getValue())
        );

        setBusy("Đang cập nhật lịch gửi...");
        AppExecutors.io().execute(() -> {
            try {
                queueService.updateQueuedEmail(selected.id, updatedEmail, newScheduledAt);
                Platform.runLater(() -> {
                    AlertUtil.showInfo("Thành công", "Đã cập nhật email trong hàng đợi.");
                    refreshQueue();
                });
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Queue task update failed. taskId={}.", selected.id, e);
                Platform.runLater(() -> {
                    statusLabel.setText("Cập nhật hàng đợi thất bại.");
                    AlertUtil.showError(
                            "Hệ thống bận",
                            "Hệ thống bận. Không thể cập nhật trạng thái hàng đợi, vui lòng thử lại sau ít giây."
                    );
                    refreshQueue();
                });
            }
        });
    }

    private void refreshQueue() {
        setBusy("Đang tải hàng đợi...");
        AppExecutors.io().execute(() -> {
            try {
                List<ScheduledEmail> emails = queueService.findScheduled();
                Platform.runLater(() -> {
                    queuedEmails.setAll(emails);
                    statusLabel.setText("Đang hiển thị " + emails.size() + " email chờ gửi.");
                });
            } catch (IOException e) {
                LOGGER.warn("Queue loading failed.", e);
                Platform.runLater(() -> {
                    statusLabel.setText("Không thể tải hàng đợi.");
                    AlertUtil.showError("Lỗi tải hàng đợi", "Không thể truy vấn danh sách email chờ gửi.");
                });
            }
        });
    }

    private ScheduledEmail selectedEmail() {
        ScheduledEmail selected = queueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Chưa chọn email", "Vui lòng chọn một email trong hàng đợi.");
        }
        return selected;
    }

    private boolean ensureCanModify(ScheduledEmail scheduledEmail) {
        if (Duration.between(LocalDateTime.now(), scheduledEmail.scheduledAt).getSeconds() < MINIMUM_LEAD_TIME_SECONDS) {
            AlertUtil.showError(
                    "Không thể thay đổi",
                    "Không thể thay đổi. Email đã đi vào trạng thái chuẩn bị gửi (dưới 60 giây trước giờ G)."
            );
            refreshQueue();
            return false;
        }
        return true;
    }

    private EditForm showEditDialog(ScheduledEmail scheduledEmail) {
        Dialog<EditForm> dialog = new Dialog<>();
        dialog.setTitle("Chỉnh sửa email chờ gửi");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField toField = new TextField(String.join(", ", scheduledEmail.email.toLst));
        TextField ccField = new TextField(String.join(", ", scheduledEmail.email.cc));
        TextField bccField = new TextField(String.join(", ", scheduledEmail.email.bcc));
        TextField subjectField = new TextField(nullToBlank(scheduledEmail.email.subject));
        TextArea bodyArea = new TextArea(nullToBlank(scheduledEmail.email.content));
        TextArea attachmentArea = new TextArea(String.join("\n", safeList(scheduledEmail.email.attachments)));
        DatePicker datePicker = new DatePicker(scheduledEmail.scheduledAt.toLocalDate());
        ComboBox<Integer> hourBox = new ComboBox<>();
        ComboBox<Integer> minuteBox = new ComboBox<>();

        for (int i = 0; i < 24; i++) {
            hourBox.getItems().add(i);
        }
        for (int i = 0; i < 60; i++) {
            minuteBox.getItems().add(i);
        }
        hourBox.setValue(scheduledEmail.scheduledAt.getHour());
        minuteBox.setValue(scheduledEmail.scheduledAt.getMinute());
        bodyArea.setPrefRowCount(8);
        attachmentArea.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        grid.addRow(0, new Label("To"), toField);
        grid.addRow(1, new Label("Cc"), ccField);
        grid.addRow(2, new Label("Bcc"), bccField);
        grid.addRow(3, new Label("Subject"), subjectField);
        grid.addRow(4, new Label("Body"), bodyArea);
        grid.addRow(5, new Label("Attachments"), attachmentArea);
        grid.addRow(6, new Label("Send at"), new HBox(8, datePicker, hourBox, new Label(":"), minuteBox));
        GridPane.setHgrow(toField, Priority.ALWAYS);
        GridPane.setHgrow(ccField, Priority.ALWAYS);
        GridPane.setHgrow(bccField, Priority.ALWAYS);
        GridPane.setHgrow(subjectField, Priority.ALWAYS);
        GridPane.setHgrow(bodyArea, Priority.ALWAYS);
        GridPane.setHgrow(attachmentArea, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            return new EditForm(toField, ccField, bccField, subjectField, bodyArea, attachmentArea, datePicker, hourBox, minuteBox);
        });
        return dialog.showAndWait().orElse(null);
    }

    private boolean validateEditForm(EditForm form) {
        boolean hasRecipient = EmailUtil.hasAnyValidEmail(form.toField.getText())
                || EmailUtil.hasAnyValidEmail(form.ccField.getText())
                || EmailUtil.hasAnyValidEmail(form.bccField.getText());
        boolean recipientsValid = EmailUtil.containsOnlyValidEmails(form.toField.getText())
                && EmailUtil.containsOnlyValidEmails(form.ccField.getText())
                && EmailUtil.containsOnlyValidEmails(form.bccField.getText());
        if (!hasRecipient || !recipientsValid) {
            AlertUtil.showError("Dữ liệu không hợp lệ", "Vui lòng nhập ít nhất một địa chỉ email hợp lệ.");
            return false;
        }

        if (form.datePicker.getValue() == null || form.hourBox.getValue() == null || form.minuteBox.getValue() == null) {
            AlertUtil.showError("Dữ liệu không hợp lệ", "Vui lòng chọn đầy đủ ngày và giờ gửi.");
            return false;
        }

        LocalDateTime scheduledAt = LocalDateTime.of(
                form.datePicker.getValue(),
                LocalTime.of(form.hourBox.getValue(), form.minuteBox.getValue())
        );
        if (scheduledAt.isBefore(LocalDateTime.now().plusSeconds(MINIMUM_LEAD_TIME_SECONDS))) {
            AlertUtil.showError("Dữ liệu không hợp lệ", "Thời gian gửi mới phải cách hiện tại tối thiểu 60 giây.");
            return false;
        }
        return true;
    }

    private List<String> attachmentPaths(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        return Arrays.stream(rawText.split("\\R"))
                .map(String::trim)
                .filter(path -> !path.isBlank())
                .collect(Collectors.toList());
    }

    private String detailFor(ScheduledEmail scheduledEmail) {
        Email email = scheduledEmail.email;
        return """
                Task ID: %s
                From: %s
                To: %s
                Cc: %s
                Bcc: %s
                Subject: %s
                Scheduled at: %s
                Attachments: %s
                """.formatted(
                scheduledEmail.id,
                LogHelper.maskEmail(email.from),
                String.join(", ", safeSet(email.toLst)),
                String.join(", ", safeSet(email.cc)),
                String.join(", ", safeSet(email.bcc)),
                nullToBlank(email.subject),
                formatDateTime(scheduledEmail.scheduledAt),
                LogHelper.attachmentCount(email)
        );
    }

    private void setBusy(String message) {
        statusLabel.setText(message);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMATTER.format(value);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private Set<String> safeSet(Set<String> values) {
        return values == null ? Set.of() : new LinkedHashSet<>(values);
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private record EditForm(
            TextField toField,
            TextField ccField,
            TextField bccField,
            TextField subjectField,
            TextArea bodyArea,
            TextArea attachmentArea,
            DatePicker datePicker,
            ComboBox<Integer> hourBox,
            ComboBox<Integer> minuteBox
    ) {
    }

    @FXML
    private void handleSearch() {

        QueueSearchCriteria criteria =
                new QueueSearchCriteria();

        criteria.setKeyword(txtKeyword.getText());
        criteria.setStatus(cbStatus.getValue());

        criteria.setSortBy(cbSortBy.getValue());
        criteria.setSortDirection(cbSortDirection.getValue());

        statusLabel.setText("Đang tìm kiếm...");

        AppExecutors.io().execute(() -> {

            try {

                List<ScheduledEmail> result =
                        queueService.search(criteria);

                Platform.runLater(() -> {

                    queuedEmails.setAll(result);

                    statusLabel.setText(
                            "Tìm thấy " + result.size() + " email."
                    );
                });

            } catch (Exception e) {

                Platform.runLater(() ->
                        AlertUtil.showError(
                                "Search Error",
                                e.getMessage()
                        )
                );
            }
        });
    }

    @FXML
    private void handleReset() {

        txtKeyword.clear();
        cbStatus.setValue(null);

        refreshQueue();
    }

    @FXML
    public void handleDeleteTask() {

        ScheduledEmail selected =
                selectedEmail();

        if (selected == null) {
            return;
        }

        Alert confirm =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Xác nhận xóa");

        confirm.setHeaderText(null);

        confirm.setContentText(
                "Bạn có chắc muốn xóa email này?"
        );

        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }

        AppExecutors.io().execute(() -> {

            try {

                queueService.delete(
                        selected.id
                );

                Platform.runLater(() -> {

                    AlertUtil.showInfo(
                            "Thành công",
                            "Đã xóa email khỏi hàng đợi."
                    );

                    refreshQueue();
                });

            } catch (Exception e) {

                Platform.runLater(() ->

                        AlertUtil.showError(
                                "Lỗi",
                                "Không thể xóa email."
                        )
                );
            }
        });
    }
}
