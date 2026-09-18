package com.example.lifestylelevellingasianlifestyle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
/** testing **/
/**
 * Main entry point for application
 */
public class Launcher extends Application {

    /**
     * Starts the application and display authentication screen
     * @param stage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception error while loading application
     */
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

    /**
     * Launches the application
     * @param args command line
     */
    public static void main(String[] args) {
        launch();
    }

}
