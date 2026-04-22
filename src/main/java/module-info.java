module precisionMail {
    requires javafx.controls;
    requires javafx.fxml;

    opens nlu.fit.soft.gr5.precisionMail to javafx.fxml;
    exports nlu.fit.soft.gr5.precisionMail;
}