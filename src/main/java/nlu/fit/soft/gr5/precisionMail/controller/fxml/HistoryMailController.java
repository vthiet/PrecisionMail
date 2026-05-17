package nlu.fit.soft.gr5.precisionMail.controller.fxml;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import nlu.fit.soft.gr5.precisionMail.service.HistoryService;
import nlu.fit.soft.gr5.precisionMail.service.impl.HistoryServiceImpl;
import nlu.fit.soft.gr5.precisionMail.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class HistoryMailController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMailController.class);

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

    private final HistoryService historyService = new HistoryServiceImpl();

    @FXML
    public void initialize() throws IOException {
        LOGGER.info("History mail screen initialization started.");
        List<Email> emails = historyService.latest();
        emailListView.getItems().addAll(emails);
        LOGGER.info("History mail loaded successfully. recordCount={}", emails.size());

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
                        LOGGER.info(
                                "History mail item selected. sender={}, recipients={}, attachments={}.",
                                LogHelper.maskEmail(email.getFrom()),
                                LogHelper.recipientCount(email),
                                LogHelper.attachmentCount(email)
                        );
                        subjectLabel.setText(email.subject);
                        fromLabel.setText("From: " + email.getFrom());
                        toLabel.setText("To: " + String.join(", ", email.toLst));

                        dateLabel.setText(email.sentAt != null ? email.sentAt.toString() : "Unknown Date");

                        contentArea.setText(email.content);
                    }
                });
    }
}
