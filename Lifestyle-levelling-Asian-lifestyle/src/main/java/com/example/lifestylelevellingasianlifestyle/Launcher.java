package com.example.lifestylelevellingasianlifestyle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Launcher.class.getResource("authenticator-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setTitle("Login / Sign Up");
        stage.setScene(scene);
        stage.show();
        IPService ipService = new IPService();

        try {
            String result = ipService.getIp();
            System.out.println("Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
