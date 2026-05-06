package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;

public class MainController {

    private final EmailService emailService = new EmailServiceImpl();

    public TextField toField;
    public TextField subjectField;
    public TextArea contentArea;
    public Button sendBtn;

    @FXML
    public void handleSendBtn() {
        boolean isValid = true;

        String to = toField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();

        if (to.isBlank() || subject.isBlank() || content.isBlank()) {
            isValid = false;
        }

        if (!isValid){
            System.out.println("invalid.");
            return;
        }

        new Thread(() -> {
            try {
                emailService.send(to, subject, content);
                System.out.println("Guiwr email thanh cong.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(TextField field, Label label, String message) {
        field.getStyleClass().add("error");
        label.setText(message);
    }

    private void clearError(TextField field, Label label) {
        field.getStyleClass().remove("error");
        label.setText("");
    }
}
