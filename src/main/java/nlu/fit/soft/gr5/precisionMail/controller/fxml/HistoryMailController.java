package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.HistorySearchCriteria;
import nlu.fit.soft.gr5.precisionMail.service.HistoryService;
import nlu.fit.soft.gr5.precisionMail.service.impl.HistoryServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryMailController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMailController.class);
    private static final int PAGE_SIZE = 50;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public TextField keywordField;
    @FXML
    public DatePicker startDatePicker;
    @FXML
    public DatePicker endDatePicker;
    @FXML
    public TableView<Email> historyTable;
    @FXML
    public TableColumn<Email, String> idColumn;
    @FXML
    public TableColumn<Email, String> recipientColumn;
    @FXML
    public TableColumn<Email, String> subjectColumn;
    @FXML
    public TableColumn<Email, String> sentAtColumn;
    @FXML
    public TableColumn<Email, String> statusColumn;
    @FXML
    public Label pageLabel;
    @FXML
    public Label statusLabel;
    @FXML
    public Button previousPageButton;
    @FXML
    public Button nextPageButton;

    private final HistoryService historyService = new HistoryServiceImpl();
    private final ObservableList<Email> emails = FXCollections.observableArrayList();
    private final Tooltip invalidDateTooltip = new Tooltip("Khoảng thời gian không hợp lệ. Ngày bắt đầu không thể lớn hơn ngày kết thúc.");
    private int pageIndex;
    private int totalRows;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().id)));
        recipientColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.join(", ", safeList(data.getValue().toLst))));
        subjectColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(nullToBlank(data.getValue().subject)));
        sentAtColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatDateTime(data.getValue().sentAt)));
        statusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().status == null ? "" : data.getValue().status.name()));

        historyTable.setItems(emails);
        historyTable.setPlaceholder(new Label("Không có dữ liệu lịch sử gửi thư."));
        historyTable.setRowFactory(table -> {
            TableRow<Email> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showSelectedDetail();
                }
            });
            return row;
        });
        loadPage(0);
    }

    @FXML
    public void handleSearch() {
        loadPage(0);
    }

    @FXML
    public void handleRefresh() {
        loadPage(pageIndex);
    }

    @FXML
    public void handlePreviousPage() {
        if (pageIndex > 0) {
            loadPage(pageIndex - 1);
        }
    }

    @FXML
    public void handleNextPage() {
        if ((pageIndex + 1) * PAGE_SIZE < totalRows) {
            loadPage(pageIndex + 1);
        }
    }

    @FXML
    public void handleViewDetail() {
        showSelectedDetail();
    }

    @FXML
    public void handleExportCsv() {
        if (emails.isEmpty()) {
            AlertUtil.showError("Không có dữ liệu", "Không có dữ liệu lịch sử để xuất báo cáo.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Xuất báo cáo lịch sử gửi thư");
        chooser.setInitialFileName("sent-history.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File target = chooser.showSaveDialog(historyTable.getScene().getWindow());
        if (target == null) {
            return;
        }

        setBusy("Đang xuất dữ liệu lịch sử...");
        List<Email> snapshot = List.copyOf(emails);
        AppExecutors.io().execute(() -> {
            try {
                Path targetPath = target.toPath();
                historyService.exportCsv(snapshot, targetPath);
                Platform.runLater(() -> {
                    statusLabel.setText("Đã xuất " + snapshot.size() + " dòng lịch sử.");
                    AlertUtil.showInfo("Xuất dữ liệu lịch sử thành công!", "Tệp đã lưu tại: " + targetPath);
                });
            } catch (IOException e) {
                LOGGER.error("History CSV export failed.", e);
                Platform.runLater(() -> {
                    statusLabel.setText("Xuất dữ liệu lịch sử thất bại.");
                    AlertUtil.showError("Lỗi xuất dữ liệu", "Không thể ghi tệp CSV. Vui lòng kiểm tra quyền ghi thư mục đã chọn.");
                });
            }
        });
    }

    private void loadPage(int requestedPageIndex) {
        HistorySearchCriteria criteria = currentCriteria();
        if (!validateDateRange(criteria)) {
            return;
        }

        clearDateError();
        setBusy("Đang tải lịch sử gửi thư...");
        long startedAt = System.nanoTime();
        AppExecutors.io().execute(() -> {
            try {
                int count = historyService.count(criteria);
                int safePage = Math.max(0, Math.min(requestedPageIndex, Math.max(0, (count - 1) / PAGE_SIZE)));
                List<Email> rows = historyService.search(criteria, safePage, PAGE_SIZE);
                long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
                LOGGER.info(
                        "User queried history. Filter params: [Keyword Length: {}, Range: {} to {}]. Results returned: [{}] rows. Duration: [{}] ms",
                        criteria.normalizedKeyword().length(),
                        criteria.startDate(),
                        criteria.endDate(),
                        rows.size(),
                        durationMs
                );
                Platform.runLater(() -> updateTable(rows, count, safePage));
            } catch (IOException e) {
                LOGGER.error("History query failed.", e);
                Platform.runLater(() -> {
                    emails.clear();
                    statusLabel.setText("Không thể kết nối cơ sở dữ liệu cục bộ. Vui lòng kiểm tra lại quyền truy cập thư mục cài đặt.");
                    pageLabel.setText("Trang 0/0");
                    updatePaginationButtons();
                });
            }
        });
    }

    private void showSelectedDetail() {
        Email selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Chưa chọn email", "Vui lòng chọn một email trong bảng lịch sử.");
            return;
        }

        setBusy("Đang tải chi tiết email...");
        AppExecutors.io().execute(() -> {
            try {
                Email detail = historyService.detail(selected.id).orElse(selected);
                String safeHtml = historyService.sanitizeHtml(detail.content);
                Platform.runLater(() -> showDetailDialog(detail, safeHtml));
            } catch (IOException e) {
                LOGGER.error("History detail query failed. id={}.", selected.id, e);
                Platform.runLater(() -> AlertUtil.showError(
                        "Lỗi đọc chi tiết",
                        "Không thể kết nối cơ sở dữ liệu cục bộ. Vui lòng kiểm tra lại quyền truy cập thư mục cài đặt."
                ));
            }
        });
    }

    private void showDetailDialog(Email email, String safeHtml) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết email lịch sử");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane metadata = new GridPane();
        metadata.setHgap(10);
        metadata.setVgap(6);
        metadata.addRow(0, new Label("To"), new Label(String.join(", ", safeList(email.toLst))));
        metadata.addRow(1, new Label("Cc"), new Label(String.join(", ", safeList(email.cc))));
        metadata.addRow(2, new Label("Bcc"), new Label(String.join(", ", safeList(email.bcc))));
        metadata.addRow(3, new Label("Tiêu đề"), new Label(nullToBlank(email.subject)));
        metadata.addRow(4, new Label("Thời gian gửi"), new Label(formatDateTime(email.sentAt)));
        metadata.addRow(5, new Label("Trạng thái"), new Label(email.status == null ? "" : email.status.name()));

        VBox attachmentsBox = new VBox(6);
        for (String attachment : safeList(email.attachments)) {
            Button button = new Button(attachment);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> openAttachment(attachment));
            attachmentsBox.getChildren().add(button);
        }
        if (attachmentsBox.getChildren().isEmpty()) {
            attachmentsBox.getChildren().add(new Label("Không có tệp đính kèm."));
        }

        WebView webView = new WebView();
        webView.setPrefSize(760, 360);
        webView.getEngine().loadContent(safeHtml);

        VBox content = new VBox(10, metadata, new Label("Tệp đính kèm"), attachmentsBox, new Label("Nội dung"), webView);
        content.setPadding(new Insets(12));
        VBox.setVgrow(webView, Priority.ALWAYS);
        dialog.getDialogPane().setContent(content);
        dialog.setOnHidden(event -> {
            webView.getEngine().loadContent("");
            System.gc();
        });
        statusLabel.setText("Đang hiển thị chi tiết email ID " + email.id + ".");
        dialog.showAndWait();
    }

    private void openAttachment(String attachment) {
        File file = new File(attachment);
        if (!file.exists()) {
            AlertUtil.showError("Không thể mở tệp", "Tệp tin gốc đã bị di dời hoặc xóa bỏ khỏi máy trạm. Không thể mở.");
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            AlertUtil.showError("Không thể mở tệp", "Hệ điều hành không hỗ trợ mở tệp trực tiếp từ ứng dụng.");
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            LOGGER.warn("Failed to open history attachment.", e);
            AlertUtil.showError("Không thể mở tệp", "Không thể mở tệp đính kèm bằng ứng dụng mặc định của hệ điều hành.");
        }
    }

    private HistorySearchCriteria currentCriteria() {
        return new HistorySearchCriteria(keywordField.getText(), startDatePicker.getValue(), endDatePicker.getValue());
    }

    private boolean validateDateRange(HistorySearchCriteria criteria) {
        LocalDate start = criteria.startDate();
        LocalDate end = criteria.endDate();
        if (start != null && end != null && start.isAfter(end)) {
            markDateError();
            statusLabel.setText("Khoảng thời gian không hợp lệ. Ngày bắt đầu không thể lớn hơn ngày kết thúc.");
            return false;
        }
        return true;
    }

    private void markDateError() {
        startDatePicker.getStyleClass().add("error");
        endDatePicker.getStyleClass().add("error");
        startDatePicker.setTooltip(invalidDateTooltip);
        endDatePicker.setTooltip(invalidDateTooltip);
    }

    private void clearDateError() {
        startDatePicker.getStyleClass().remove("error");
        endDatePicker.getStyleClass().remove("error");
        startDatePicker.setTooltip(null);
        endDatePicker.setTooltip(null);
    }

    private void updateTable(List<Email> rows, int count, int safePage) {
        emails.setAll(rows);
        totalRows = count;
        pageIndex = safePage;
        int totalPages = count == 0 ? 0 : ((count - 1) / PAGE_SIZE) + 1;
        pageLabel.setText("Trang " + (count == 0 ? 0 : pageIndex + 1) + "/" + totalPages);
        statusLabel.setText("Đang hiển thị " + rows.size() + "/" + count + " email lịch sử.");
        updatePaginationButtons();
    }

    private void updatePaginationButtons() {
        previousPageButton.setDisable(pageIndex <= 0);
        nextPageButton.setDisable((pageIndex + 1) * PAGE_SIZE >= totalRows);
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

    private List<String> safeList(Iterable<String> values) {
        if (values == null) {
            return List.of();
        }
        ObservableList<String> result = FXCollections.observableArrayList();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }
}
