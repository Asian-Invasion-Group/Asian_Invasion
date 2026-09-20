package com.example.lifestylelevellingasianlifestyle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/** testing **/
/**
 * Main entry point for application
 */
public class Launcher extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Database.initialize();
            showAuthentication(stage);
        } catch (SQLException | IOException e) {
            System.err.println(
                    "Application startup failed: "
                            + e.getClass().getSimpleName()
            );

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lifestyle Levelling");
            alert.setHeaderText("The application could not start");
            alert.setContentText(
                    "Local storage could not be prepared. " +
                    "Check that the data folder is writable, " +
                    "then restart the application."
            );
            alert.showAndWait();
        }
    }

    public static void showAuthentication(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(
                Launcher.class.getResource(
                        "authenticator-view.fxml"
                )
        );

        Scene scene = new Scene(
                fxmlLoader.load(),
                480,
                600
        );

        stage.setTitle("Lifestyle Levelling");
        stage.setMinWidth(440);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        launch();
    }
}