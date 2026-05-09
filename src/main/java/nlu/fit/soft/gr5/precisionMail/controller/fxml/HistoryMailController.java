package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;

import java.io.IOException;
import java.util.List;

public class HistoryMailController {

    // 1. Added <Email> generic type to ListView
    @FXML
    public ListView<Email> emailListView;

    @FXML
    public Label subjectLabel;
    @FXML
    public Label fromLabel;
    @FXML
    public Label toLabel;
    @FXML
    public Label dateLabel;
    @FXML
    public TextArea contentArea;

    private final EmailService emailService = new EmailServiceImpl();

    @FXML
    public void initialize() throws IOException {
        List<Email> emails = emailService.findAll();
        emailListView.getItems().addAll(emails);

        emailListView.setCellFactory(param -> new ListCell<Email>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);

                if (empty || email == null) {
                    setText(null);
                } else {
                    setText(email.subject + "\n" + email.getFrom());
                }
            }
        });

        emailListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, email) -> {
                    if (email != null) {
                        subjectLabel.setText(email.subject);
                        fromLabel.setText("From: " + email.getFrom());
                        toLabel.setText("To: " + String.join(", ", email.toLst));

                        dateLabel.setText(email.sentAt != null ? email.sentAt.toString() : "Unknown Date");

                        contentArea.setText(email.content);
                    }
                });
    }
}