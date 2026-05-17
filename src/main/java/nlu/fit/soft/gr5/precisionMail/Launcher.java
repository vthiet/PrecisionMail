package nlu.fit.soft.gr5.precisionMail;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.infrastructure.async.AppExecutors;
import nlu.fit.soft.gr5.precisionMail.infrastructure.db.DatabaseInitializer;
import nlu.fit.soft.gr5.precisionMail.service.ApplicationStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class Launcher extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    @Override
    public void start(Stage stage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                LOGGER.error("Unhandled runtime exception on thread={}", thread.getName(), throwable)
        );

        URL urlResource = Launcher.class.getResource(
                "view/MainScreen.fxml"
        );
        if (urlResource == null) {
            throw new IOException("Cannot load MainScreen.fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(urlResource);
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("Precision Mail");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if (ApplicationStateService.hasActiveEmailSend()) {
                event.consume();
                LOGGER.warn("Application close rejected because an email send is still active.");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Đang gửi email");
                alert.setHeaderText(null);
                alert.setContentText("Email đang được gửi. Vui lòng chờ quá trình gửi hoàn tất trước khi tắt ứng dụng.");
                alert.showAndWait();
            }
        });
        stage.show();
        LOGGER.info("Application started successfully.");
    }

    public static void main(String[] args){
        LOGGER.info("Application bootstrap started.");
        DatabaseInitializer.initialize();
        Application.launch(Launcher.class, args);
    }

    @Override
    public void stop() {
        AppExecutors.shutdown();
    }
}
