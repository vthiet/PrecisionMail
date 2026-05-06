package nlu.fit.soft.gr5.precisionMail;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class   HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        URL resource = HelloApplication.class.getResource(
                "view/MailView.fxml"
        );
        System.out.println(resource);
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Scene scene = new Scene(fxmlLoader.load(), 500, 240);

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
