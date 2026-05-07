package nlu.fit.soft.gr5.precisionMail.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import nlu.fit.soft.gr5.precisionMail.service.NavigationService;

import java.io.IOException;

public class MainScreenController {

    @FXML
    public BorderPane mainBorderPane;

    @FXML
    public void initialize(){
        NavigationService.getInstance().setNavigationListener(this::changeCenterView);
    }

    private void changeCenterView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nlu/fit/soft/gr5/precisionMail/view/include/" + fxmlFileName));
            Parent newView = loader.load();
            mainBorderPane.setCenter(newView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("File not found: " + fxmlFileName);
        }
    }
}
