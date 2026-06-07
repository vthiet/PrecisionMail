package nlu.fit.soft.gr5.precisionMail.controller.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import nlu.fit.soft.gr5.precisionMail.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Controller for Email Preview Dialog
 * Displays a comprehensive preview of the email before sending
 * Including: From, To, Cc, Bcc, Subject, Body (HTML), and Attachments
 */
public class PreviewEmailController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewEmailController.class);

    @FXML
    public Label fromLabel;
    @FXML
    public Label toLabel;
    @FXML
    public Label ccLabel;
    @FXML
    public Label bccLabel;
    @FXML
    public Label subjectLabel;
    @FXML
    public WebView contentWebView;
    @FXML
    public ListView<File> attachmentListView;
    @FXML
    public Label attachmentInfoLabel;
    @FXML
    public VBox ccSection;
    @FXML
    public VBox bccSection;
    @FXML
    public VBox attachmentSection;

    private Email email;

    @FXML
    public void initialize() {
        // Setup attachment list view cell factory
        attachmentListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                } else {
                    // Display file name with size
                    long sizeKB = file.length() / 1024;
                    String sizeStr = sizeKB < 1024 ? sizeKB + " KB" : String.format("%.1f MB", sizeKB / 1024.0);
                    setText(file.getName() + " (" + sizeStr + ")");
                }
            }
        });
    }

    /**
     * Populate preview with email data
     */
    public void setEmail(Email email, String senderEmail, List<File> attachments) {
        this.email = email;

        // Set From
        fromLabel.setText(senderEmail);

        // Set To
        if (email.toLst != null && !email.toLst.isEmpty()) {
            toLabel.setText(String.join(", ", email.toLst));
        } else {
            toLabel.setText("[No recipients]");
        }

        // Set Cc (hide section if empty)
        if (email.cc != null && !email.cc.isEmpty()) {
            ccLabel.setText(String.join(", ", email.cc));
            ccSection.setVisible(true);
            ccSection.setManaged(true);
        } else {
            ccSection.setVisible(false);
            ccSection.setManaged(false);
        }

        // Set Bcc (hide section if empty)
        if (email.bcc != null && !email.bcc.isEmpty()) {
            bccLabel.setText(String.join(", ", email.bcc));
            bccSection.setVisible(true);
            bccSection.setManaged(true);
        } else {
            bccSection.setVisible(false);
            bccSection.setManaged(false);
        }

        // Set Subject
        String subject = email.subject != null && !email.subject.isEmpty() ? email.subject : "[No Subject]";
        subjectLabel.setText(subject);

        // Set Body (HTML content)
        if (email.content != null && !email.content.isEmpty()) {
            // Wrap content in HTML with base styling
            String htmlContent = String.format(
                    "<html><head><meta charset='UTF-8'/><style>body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 13px; color: #202124; margin: 12px; }</style></head><body>%s</body></html>",
                    email.content
            );
            contentWebView.getEngine().loadContent(htmlContent);
        } else {
            contentWebView.getEngine().loadContent("<html><body><em>[Empty message]</em></body></html>");
        }

        // Set Attachments
        if (attachments != null && !attachments.isEmpty()) {
            attachmentListView.getItems().setAll(attachments);
            attachmentInfoLabel.setText(attachments.size() + " Attachment" + (attachments.size() > 1 ? "s" : ""));
            attachmentSection.setVisible(true);
            attachmentSection.setManaged(true);

            LOGGER.info("Preview attachments loaded: count={}", attachments.size());
        } else {
            attachmentSection.setVisible(false);
            attachmentSection.setManaged(false);
        }
    }

    /**
     * Get the email object
     */
    public Email getEmail() {
        return email;
    }
}
