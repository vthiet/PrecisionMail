package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.model.LogEntry;
import nlu.fit.soft.gr5.precisionMail.service.LogMonitoringService;
import nlu.fit.soft.gr5.precisionMail.service.impl.LogMonitoringServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.AlertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class LogController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogController.class);
    private static final int MAX_DISPLAY_LINES = 1000;

    @FXML
    public ComboBox<String> levelComboBox;
    @FXML
    public TextField keywordField;
    @FXML
    public TableView<LogEntry> logTable;
    @FXML
    public TableColumn<LogEntry, String> timestampColumn;
    @FXML
    public TableColumn<LogEntry, String> levelColumn;
    @FXML
    public TableColumn<LogEntry, String> threadColumn;
    @FXML
    public TableColumn<LogEntry, String> sourceColumn;
    @FXML
    public TableColumn<LogEntry, String> messageColumn;
    @FXML
    public TextArea detailArea;
    @FXML
    public Label statusLabel;

    private final LogMonitoringService logMonitoringService = new LogMonitoringServiceImpl();
    private final ObservableList<LogEntry> allEntries = FXCollections.observableArrayList();
    private final ObservableList<LogEntry> visibleEntries = FXCollections.observableArrayList();
    private LogMonitoringService.LogWatchRegistration watchRegistration;

    @FXML
    public void initialize() {
        levelComboBox.setItems(FXCollections.observableArrayList("ALL", "ERROR", "WARN", "INFO", "DEBUG"));
        levelComboBox.getSelectionModel().select("ALL");
        levelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        keywordField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());

        timestampColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().timestamp()));
        levelColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().level()));
        threadColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().thread()));
        sourceColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().source()));
        messageColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().message()));

        logTable.setItems(visibleEntries);
        logTable.setPlaceholder(new Label("Không có dữ liệu log để hiển thị."));
        logTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) ->
                detailArea.setText(selected == null ? "" : selected.rawLine()));
        logTable.setRowFactory(table -> {
            TableRow<LogEntry> row = new TableRow<>();
            row.itemProperty().addListener((observable, previous, current) -> styleRow(row, current));
            return row;
        });

        LOGGER.debug("Log monitoring UI initialized. Streaming last [{}] lines from file [{}].",
                MAX_DISPLAY_LINES,
                logMonitoringService.activeLogFile());
        handleRefresh();
        startWatcher();
    }

    @FXML
    public void handleRefresh() {
        setStatus("Đang tải 1000 dòng log mới nhất...");
        AppExecutors.io().execute(() -> {
            try {
                List<LogEntry> entries = logMonitoringService.readRecentLines(MAX_DISPLAY_LINES)
                        .stream()
                        .map(LogEntry::parse)
                        .toList();
                boolean oversized = logMonitoringService.isActiveLogOversized();
                Platform.runLater(() -> {
                    allEntries.setAll(entries);
                    applyFilter();
                    setStatus(oversized
                            ? "Tệp nhật ký quá lớn. Hệ thống chỉ hiển thị 1000 dòng mới nhất để tối ưu hóa hiệu năng."
                            : "Đang hiển thị " + visibleEntries.size() + "/" + allEntries.size() + " dòng log.");
                    statusLabel.setTooltip(oversized
                            ? new Tooltip("Tệp nhật ký quá lớn. Hệ thống chỉ hiển thị 1000 dòng mới nhất để tối ưu hóa hiệu năng.")
                            : null);
                });
            } catch (IOException e) {
                LOGGER.error("Failed to read active log file.", e);
                Platform.runLater(() -> {
                    allEntries.clear();
                    visibleEntries.clear();
                    setStatus("Không thể truy cập tệp tin nhật ký hệ thống cục bộ. Vui lòng kiểm tra lại quyền ghi tệp trong thư mục cài đặt.");
                });
            }
        });
    }

    @FXML
    public void handleFilterLogs() {
        setStatus("Đang lọc dữ liệu log...");
        String level = levelComboBox.getValue();
        String keyword = keywordField.getText();
        AppExecutors.io().execute(() -> {
            try {
                List<LogEntry> entries = logMonitoringService.streamAndFilterLogs(level, keyword, MAX_DISPLAY_LINES)
                        .stream()
                        .map(LogEntry::parse)
                        .toList();
                Platform.runLater(() -> {
                    visibleEntries.setAll(entries);
                    setStatus("Đang hiển thị " + visibleEntries.size() + " dòng log đã lọc.");
                });
            } catch (IOException e) {
                LOGGER.error("Failed to filter active log file.", e);
                Platform.runLater(() -> setStatus("Không thể lọc tệp nhật ký hệ thống cục bộ."));
            }
        });
    }

    @FXML
    public void handleOpenLogFolder() {
        Path logDir = logMonitoringService.activeLogDirectory();
        setStatus("Đang mở thư mục log...");
        AppExecutors.io().execute(() -> {
            try {
                Files.createDirectories(logDir);
                openDirectory(logDir);
                Platform.runLater(() -> setStatus("Đã mở thư mục log: " + logDir));
            } catch (IOException e) {
                LOGGER.warn("Failed to open log directory [{}].", logDir, e);
                Platform.runLater(() -> {
                    setStatus("Không thể mở thư mục log: " + logDir);
                    AlertUtil.showError("Không thể mở thư mục", "Không thể mở thư mục chứa log bằng trình quản lý tệp mặc định.");
                });
            }
        });
    }

    @FXML
    public void handleExportLog() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Xuất file Log");
        chooser.setInitialFileName("precisionmail-log.zip");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files", "*.zip"));
        File target = chooser.showSaveDialog(logTable.getScene().getWindow());
        if (target == null) {
            return;
        }

        setStatus("Đang xuất tập tin nhật ký kỹ thuật...");
        AppExecutors.io().execute(() -> {
            try {
                Path exportedPath = logMonitoringService.exportActiveLogs(target.toPath());
                Platform.runLater(() -> {
                    setStatus("Đã xuất tập tin nhật ký kỹ thuật: " + exportedPath);
                    AlertUtil.showInfo("Xuất tập tin nhật ký kỹ thuật thành công!", "Tệp đã lưu tại: " + exportedPath);
                });
            } catch (IOException e) {
                LOGGER.error("Log export failed.", e);
                Platform.runLater(() -> {
                    setStatus("Xuất tập tin nhật ký thất bại.");
                    AlertUtil.showError("Lỗi xuất file Log", "Không thể xuất tệp log. Vui lòng kiểm tra quyền ghi thư mục đã chọn.");
                });
            }
        });
    }

    public void shutdown() {
        if (watchRegistration != null) {
            watchRegistration.close();
            watchRegistration = null;
        }
    }

    private void startWatcher() {
        AppExecutors.io().execute(() -> {
            try {
                watchRegistration = logMonitoringService.watchActiveLog(lines ->
                        Platform.runLater(() -> appendLiveEntries(lines)));
            } catch (IOException e) {
                LOGGER.warn("Could not start active log watcher.", e);
                Platform.runLater(() -> setStatus("Không thể kích hoạt theo dõi log thời gian thực."));
            }
        });
    }

    private void appendLiveEntries(List<String> lines) {
        for (String line : lines) {
            allEntries.add(LogEntry.parse(line));
            if (allEntries.size() > MAX_DISPLAY_LINES) {
                allEntries.remove(0);
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        String selectedLevel = levelComboBox.getValue();
        String keyword = keywordField.getText() == null ? "" : keywordField.getText().trim().toLowerCase(Locale.ROOT);
        visibleEntries.setAll(allEntries.stream()
                .filter(entry -> selectedLevel == null || "ALL".equals(selectedLevel) || selectedLevel.equalsIgnoreCase(entry.level()))
                .filter(entry -> keyword.isBlank() || entry.rawLine().toLowerCase(Locale.ROOT).contains(keyword))
                .toList());
        setStatus("Đang hiển thị " + visibleEntries.size() + "/" + allEntries.size() + " dòng log.");
    }

    private void styleRow(TableRow<LogEntry> row, LogEntry entry) {
        row.getStyleClass().removeAll("log-row-error", "log-row-warn", "log-row-info", "log-row-debug");
        if (entry == null) {
            return;
        }
        switch (entry.level().toUpperCase(Locale.ROOT)) {
            case "ERROR" -> row.getStyleClass().add("log-row-error");
            case "WARN" -> row.getStyleClass().add("log-row-warn");
            case "INFO" -> row.getStyleClass().add("log-row-info");
            case "DEBUG" -> row.getStyleClass().add("log-row-debug");
            default -> {
            }
        }
    }

    private void openDirectory(Path directory) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(directory.toFile());
                return;
            } catch (IOException e) {
                LOGGER.debug("Desktop API could not open log directory. Falling back to OS command.", e);
            }
        }

        IOException failure = null;
        for (List<String> command : openDirectoryCommands(directory)) {
            try {
                new ProcessBuilder(command).start();
                return;
            } catch (IOException e) {
                failure = e;
            }
        }

        if (failure != null) {
            throw failure;
        }
        throw new IOException("No supported command found for opening directory.");
    }

    private List<List<String>> openDirectoryCommands(Path directory) {
        String path = directory.toAbsolutePath().toString();
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return List.of(List.of("explorer", path));
        }
        if (os.contains("mac")) {
            return List.of(List.of("open", path));
        }
        return List.of(
                List.of("xdg-open", path),
                List.of("gio", "open", path),
                List.of("kde-open", path),
                List.of("gnome-open", path)
        );
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
