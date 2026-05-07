package nlu.fit.soft.gr5.precisionMail;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL urlResource = Launcher.class.getResource(
                "view/MainScreen.fxml"
        );
        FXMLLoader fxmlLoader = new FXMLLoader(urlResource);
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    static void main(String[] args) {
        Application.launch(Launcher.class, args);
    }
}
