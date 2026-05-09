package nlu.fit.soft.gr5.precisionMail;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nlu.fit.soft.gr5.precisionMail.util.DbUtil;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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

    static void main(String[] args){
        initDatabase();
        Application.launch(Launcher.class, args);
    }

    private static void initDatabase() {
        String sql = """
                create table if not exists accounts
                (
                    id integer primary key autoincrement,
                    username text not null,
                    password text not null,
                    created_at text not null
                );
                """;

        try (Connection connection = DbUtil.getConnect();
             Statement st = connection.createStatement()) {

            st.execute(sql);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
