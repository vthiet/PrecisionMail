package nlu.fit.soft.gr5.precisionMail;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        LOGGER.info("Application started successfully.");
    }

    public static void main(String[] args){
        LOGGER.info("Application bootstrap started.");
        initDatabase();
        Application.launch(Launcher.class, args);
    }

    private static void initDatabase() {
        try (Connection connection = DbUtil.getConnect();
             Statement st = connection.createStatement()) {
            st.execute("""
                    create table if not exists accounts
                    (
                        id integer primary key autoincrement,
                        email text not null unique,
                        encrypt_app_password text not null,
                        created_at text not null,
                        updated_at text not null
                    );
                    """);
            LOGGER.info("Database initialization completed successfully.");

        } catch (SQLException e) {
            LOGGER.error("Database initialization failed.", e);
            throw new RuntimeException(e);
        }
    }
}
