package com.loltournamentplanner;

import com.loltournamentplanner.db.DbConfig;
import com.loltournamentplanner.db.DbInit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            DbInit.ensureSchema();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Cannot connect to MySQL / initialize schema");
            alert.setContentText(
                    "Start MySQL (XAMPP) and verify connection settings.\n" +
                    "DB_URL=" + DbConfig.url() + "\n" +
                    "DB_USER=" + DbConfig.user() + "\n\n" +
                    "Error: " + e.getMessage()
            );
            alert.showAndWait();
            Platform.exit();
            return;
        }

        scene = new Scene(loadFXML("view/login-view"), 1000, 800);
        stage.setTitle("LoL Tournament Planner");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
