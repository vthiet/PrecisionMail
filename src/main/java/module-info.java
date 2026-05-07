module precisionMail {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.mail;

    opens nlu.fit.soft.gr5.precisionMail to javafx.fxml;
    opens nlu.fit.soft.gr5.precisionMail.controller to javafx.fxml;

    exports nlu.fit.soft.gr5.precisionMail;
    exports nlu.fit.soft.gr5.precisionMail.controller;
}