module precisionMail {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jakarta.mail;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;

    opens nlu.fit.soft.gr5.precisionMail to javafx.fxml;
    opens nlu.fit.soft.gr5.precisionMail.controller to javafx.fxml;
    opens nlu.fit.soft.gr5.precisionMail.model to com.fasterxml.jackson.databind;

    exports nlu.fit.soft.gr5.precisionMail;
    exports nlu.fit.soft.gr5.precisionMail.model;
    exports nlu.fit.soft.gr5.precisionMail.controller;
    exports nlu.fit.soft.gr5.precisionMail.controller.dialog;
    opens nlu.fit.soft.gr5.precisionMail.controller.dialog to javafx.fxml;
    exports nlu.fit.soft.gr5.precisionMail.controller.fxml;
    opens nlu.fit.soft.gr5.precisionMail.controller.fxml to javafx.fxml;
    exports nlu.fit.soft.gr5.precisionMail.util;
}
