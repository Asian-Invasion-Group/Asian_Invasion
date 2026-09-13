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
        WeatherService weatherService = new WeatherService();

        LocationData location = ipService.getIp();
        AIService aiService = new AIService();
        String text = weatherService.getWeather(location.latitude(), location.longitude());
        System.out.println(location.cityName());
        System.out.println(location.latitude());
        System.out.println(location.longitude());
        System.out.println(aiService.queryAI("Why is the sky blue?"));
    }

    public static void main(String[] args) {
        launch();
    }

}
