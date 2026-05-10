package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import jakarta.mail.MessagingException;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class ComposeMailController {
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

    private Account currentAccount = null;
    private final ObservableList<File> attachments = FXCollections.observableArrayList();

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
                } else {
                    // Create HBox with file name and delete button
                    HBox hbox = new HBox(10);
                    hbox.setPadding(new Insets(5));
                    hbox.setStyle("-fx-alignment: CENTER_LEFT;");

                    Label fileLabel = new Label(file.getName());
                    fileLabel.setStyle("-fx-text-fill: #333333;");

                    Pane spacer = new Pane();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button deleteBtn = new Button("×");
                    deleteBtn.setPrefSize(25, 25);
                    deleteBtn.setStyle("-fx-font-size: 16; -fx-padding: 0; -fx-text-fill: #d9534f;");
                    deleteBtn.setOnAction(e -> deleteAttachment(file));

                    hbox.getChildren().addAll(fileLabel, spacer, deleteBtn);
                    setGraphic(hbox);
                }
            }
        });

        attachmentCountLabel.textProperty().bind(
                Bindings.size(attachments)
                        .asString("%d Attachment(s)")
        );

        attachmentSizeLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {

                    long total = attachments.stream()
                            .mapToLong(File::length)
                            .sum();

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
            updateMenu(loadAccountService.getValue());
        });

        loadAccountService.setOnFailed(e -> {
            accountMenuButton.setText("Tải tài khoản thất bại.");
            loadAccountService.getException().printStackTrace();
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
        Account account = currentAccount;

        if (currentAccount == null) {
            AlertUtil.showError("Lỗi", "Vui lòng chọn tài khoản để gửi email.");
            return;
        }

        Set<String> toLst = EmailUtil.emailFeature(toField.getText());
        Set<String> ccLst = EmailUtil.emailFeature(ccField.getText());
        Set<String> bccLst = EmailUtil.emailFeature(bccField.getText());
        String subject = subjectField.getText();
        String content = contentArea.getText();

        // Convert File objects to absolute paths
        List<String> attachmentPaths = null;
        if (!attachments.isEmpty()) {
            attachmentPaths = attachments.stream()
                    .map(File::getAbsolutePath)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Create and send email
        Email email = new Email(account.getUsername(),
                toLst, ccLst, bccLst, subject, content, attachmentPaths, LocalDateTime.now());

        emailService.send(account, email);

        // Clear form after successful send
        clearTextInput(toField, ccField, bccField, subjectField, contentArea);
        attachments.clear();

        AlertUtil.showInfo("Thành công", "Email đã được gửi!");
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
            });

            accountMenuButton.getItems().add(item);

            if (first) {
                item.setSelected(true);
                currentAccount = account;
                accountMenuButton.setText(account.getUsername());
                first = false;
            }
        }

        accountMenuButton.getItems().add(new SeparatorMenuItem());
        accountMenuButton.getItems().add(new MenuItem("Customize From Address..."));
    }

    private void clearTextInput(TextInputControl... inputLst) {
        if (inputLst != null) {
            for (var input : inputLst) {
                input.clear();
            }
        }
    }

    public void handleAttachFiles(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(
                attachmentContainer.getScene().getWindow()
        );

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }

        // Validate and add files one by one
        for (File file : selectedFiles) {
            AttachmentValidator.ValidationResult validation =
                    AttachmentValidator.validateFileAddition(file, attachments);

            if (validation.isValid) {
                attachments.add(file);
            } else {
                // Show error alert for failed file
                AlertUtil.showError("Không thể thêm file", validation.errorMessage);
            }
        }
    }

    public void deleteAttachment(File file) {
        attachments.remove(file);
    }

    private String formatSize(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        return String.format("%.1f MB", mb);
    }

    public void handleScheduleSendMail(ActionEvent actionEvent) {
        LocalDate date = scheduleDatePicker.getValue();

        Integer hour = hourBox.getValue();
        Integer minute = minuteBox.getValue();

        LocalDateTime scheduledAt = LocalDateTime.of(date, LocalTime.of(hour, minute));


        Account account = currentAccount;

        if (currentAccount == null) {
            AlertUtil.showError("Lỗi", "Vui lòng chọn tài khoản để gửi email.");
            return;
        }

        Set<String> toLst = EmailUtil.emailFeature(toField.getText());
        Set<String> ccLst = EmailUtil.emailFeature(ccField.getText());
        Set<String> bccLst = EmailUtil.emailFeature(bccField.getText());
        String subject = subjectField.getText();
        String content = contentArea.getText();

        // Convert File objects to absolute paths
        List<String> attachmentPaths = null;
        if (!attachments.isEmpty()) {
            attachmentPaths = attachments.stream()
                    .map(File::getAbsolutePath)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Create and send email
        Email email = new Email(account.getUsername(),
                toLst, ccLst, bccLst, subject, content, attachmentPaths, LocalDateTime.now());

        ScheduledEmail scheduledEmail = new ScheduledEmail(account, email, scheduledAt);

        scheduledEmailService.schedule(scheduledEmail);

        // Clear form after successful send
        clearTextInput(toField, ccField, bccField, subjectField, contentArea);
        attachments.clear();

        AlertUtil.showInfo("Thành công", "Email đã được gửi!");
    }
}