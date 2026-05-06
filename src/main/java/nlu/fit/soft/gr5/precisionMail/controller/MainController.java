package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import nlu.fit.soft.gr5.precisionMail.service.EmailService;
import nlu.fit.soft.gr5.precisionMail.service.impl.EmailServiceImpl;

public class MainController {

    private final EmailService emailService = new EmailServiceImpl();

    public TextField toField;
    public TextField subjectField;
    public TextArea contentArea;
    public Button sendBtn;
    public Label toError;
    public Label subjectError;
    public TextField bccField;
    public TextField ccField;
    public Button ccButton;
    public Button bccButton;
    public Button clearBtn;

    @FXML
    public void handleSendBtn() {
        boolean isValid = true;

        toError.setText("");
        subjectError.setText("");
        toField.getStyleClass().remove("error");
        subjectField.getStyleClass().remove("error");

        String to = toField.getText();
        String subject = subjectField.getText();
        String content = contentArea.getText();

        if (to.isBlank()) {
            toError.setText("Vui lòng nhập người nhận");
            toField.getStyleClass().add("error");
            isValid = false;
        }

        if (subject.isBlank()) {
            subjectError.setText("Vui lòng nhập tiêu đề");
            subjectField.getStyleClass().add("error");
            isValid = false;
        }

        if (!isValid) return;

        new Thread(() -> {
            try {
                emailService.send(to, subject, content);
                System.out.println("Email sent successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        clearContent(toField, subjectField, contentArea);
    }

    private void clearContent(TextInputControl... nodes){
        for (var node : nodes) node.clear();
    }

    @FXML
    public void initialize() {
        /*
        if toError is empty then toError visible = false
        if toError visible = false then managed = false, else then
         */
        toError.visibleProperty().bind(toError.textProperty().isNotEmpty());
        toError.managedProperty().bind(toError.visibleProperty());

        subjectError.visibleProperty().bind(subjectError.textProperty().isNotEmpty());
        subjectError.managedProperty().bind(subjectError.visibleProperty());

        ccField.managedProperty().bind(ccField.visibleProperty());
        bccField.managedProperty().bind(bccField.visibleProperty());

        ccButton.visibleProperty().bind(ccField.visibleProperty().not());
        ccButton.managedProperty().bind(ccButton.visibleProperty());

        bccButton.visibleProperty().bind(bccField.visibleProperty().not());
        bccButton.managedProperty().bind(bccButton.visibleProperty());
    }

    @FXML
    public void handleClearBtn(ActionEvent actionEvent) {
        toError.setText("");
        subjectError.setText("");
        toField.getStyleClass().remove("error");
        subjectField.getStyleClass().remove("error");
        clearContent(toField, subjectField, contentArea);
    }

    @FXML
    public void handleToggleCC(){
        ccField.setVisible(true);
    }

    @FXML
    public void handleToggleBCC(){
        bccField.setVisible(true);
    }
}
