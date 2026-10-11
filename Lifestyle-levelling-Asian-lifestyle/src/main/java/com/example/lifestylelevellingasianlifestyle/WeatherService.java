package com.example.lifestylelevellingasianlifestyle;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * Class to retrieve and process weather information through an external weather service
 * Done through the API provided
 */
public class WeatherService {

    @FXML
    public Button groceryButton;
    @FXML
    public Button homeButton;
    @FXML
    public Button utilityButton;
    @FXML
    public Button weatherButton;

    /**
     * Retrieves the current weather for a specified location
     * @param latitude lat coordinate of current user
     * @param longitude long coordinate of current user
     * @return a string containing result
     */
    public void getWeather(double latitude, double longitude) {

        String urlString = String.format(
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=%f" +
                        "&longitude=%f" +
                        "&hourly=temperature_2m,rain,weather_code" +
                        "&current=temperature_2m,precipitation,rain,weather_code,is_day" +
                        "&forecast_days=3",
                latitude,
                longitude
        );

        try {
            URL url = URI.create(urlString).toURL();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setConnectTimeout(2500);
            con.setReadTimeout(2500);

            int responseCode = con.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Weather API returned: " + responseCode);
                //return null;
            }

            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }


            JSONObject jsonObject = new JSONObject(jsonBuffer.toString());
            JSONObject curently = jsonObject.getJSONObject("current");
            String simpleCurrentWeather = resolveCode(curently.getInt("weather_code"));
            boolean isDay = intToBooleanWithThrowing((curently.getInt("is_day")));
            int currentTemp = curently.getInt("temperature_2m");
            int currentWeatherCode = curently.getInt("weather_code");
            String[] recommendations = getRecommendations(jsonBuffer.toString());

            JSONObject hourly = jsonObject.getJSONObject("hourly");
            JSONArray times =hourly.getJSONArray("time");
            JSONArray temperatures = hourly.getJSONArray("temperature_2m");


            String[] temperaturess = getRecommendations(jsonBuffer.toString());

            System.out.println("It is currently " + currentTemp + " degrees with " +
                    simpleCurrentWeather + "!");
            System.out.println(Arrays.toString(temperaturess));

            currentWeatherText.setText(simpleCurrentWeather + " " + currentTemp + " °C");
            String path = "/Images/weatherImages/" + currentWeatherCode + ".png";

            InputStream imageStream = getClass().getResourceAsStream(path);

            if (imageStream != null) {
                Image newImage = new Image(imageStream);
                currentWeatherImage.setImage(newImage);
            } else {
                System.out.println("Image not found: " + path);
                InputStream backup = getClass().getResourceAsStream("/Images/weatherImages/0.png");
                Image newImage = new Image(backup);
                currentWeatherImage.setImage(newImage);
            }
            con.disconnect();
            //return "-1";
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        }
    }

    /**
     * Converts weather code into a weather description
     * @param weatherCode numerical code given by the external service
     * @return a pre-written response corresponding to the code
     */
    private String resolveCode(int weatherCode) {
        String weatherType;
        switch (weatherCode) {
            case 0:
                weatherType = "Clear Sky";
                break;
            case 1:
                weatherType = "Mainly Clear";
                break;
            case 2:
                weatherType = "Partly Cloudy";
                break;
            case 3:
                weatherType = "Overcast";
                break;
            case 45:
                weatherType = "Fog";
                break;
            case 48:
                weatherType = "Depositing Rime Fog";
                break;
            case 51:
                weatherType = "Light Drizzle";
                break;
            case 55:
                weatherType = "Intense Drizzle";
                break;
            case 53:
                weatherType = "Moderate Drizzle";
                break;
            case 56:
                weatherType = "Light Freezing Drizzle";
                break;
            case 57:
                weatherType = "Dense Freezing Drizzle";
                break;
            case 61:
                weatherType = "Slight Rain";
                break;
            case 63:
                weatherType = "Moderate Rain";
                break;
            case 65:
                weatherType = "Heavy Rain";
                break;
            case 66:
                weatherType = "Light Freezing Rain";
                break;
            case 67:
                weatherType = "Heavy Freezing Rain";
                break;
            case 71:
                weatherType = "Slight SnowFall";
                break;
            case 73:
                weatherType = "Moderate SnowFall";
                break;
            case 75:
                weatherType = "Heavy SnowFall";
                break;
            case 77:
                weatherType = "Snow Grains";
                break;
            case 80:
                weatherType = "Slight Rain Showers";
                break;
            case 81:
                weatherType = "Moderate Rain Showers";
                break;
            case 82:
                weatherType = "Heavy Rain Showers";
                break;
            case 85:
                weatherType = "Slight Snow Showers";
                break;
            case 86:
                weatherType = "Heavy Snow Showers";
                break;
            case 95:
                weatherType = "ThunderStorm";
                break;
            case 96:
                weatherType = "ThunderStorm with Sligh Hail";
                break;
            case 99:
                weatherType = "ThunderStorm with Heavy Hail";
                break;
            default:
                System.out.println("Invalid Code");
                weatherType = "";
        }
        //System.out.println(weatherType);
        return weatherType;
    }

    /**
     * Converts integer to a boolean
     * @param theInt integert to convert, represents a boolean
     * @return boolean values, true or false
     */
    boolean intToBooleanWithThrowing(int theInt) {
        if (theInt == 1) {
            return true;
        }
        if (theInt == 0) {
            return false;
        }
        throw new IllegalArgumentException("Invalid input, Only 0 or 1 is allowed.");
    }

    public String[] getRecommendations(String json) {
        JSONObject jsonObject = new JSONObject(json);

        JSONObject current = jsonObject.getJSONObject("current");
        String simpleCurrentWeather = resolveCode(current.getInt("weather_code"));
        boolean isDay = intToBooleanWithThrowing(current.getInt("is_day"));
        int currentTemp = current.getInt("temperature_2m");
        int currentWeatherCode = current.getInt("weather_code");

        JSONObject hourly = jsonObject.getJSONObject("hourly");

        JSONArray time = hourly.getJSONArray("time");
        JSONArray weatherCode = hourly.getJSONArray("weather_code");
        JSONArray rain = hourly.getJSONArray("rain");
        JSONArray temperatures = hourly.getJSONArray("temperature_2m");

        String[] temperatureArray = new String[temperatures.length()];

        boolean goodWeather = false;
        String goodWeatherStart = null;
        String goodWeatherEnd = null;

        for (int i = 0; i < temperatures.length(); i++) {
            int temp = temperatures.getInt(i);
            int code = weatherCode.getInt(i);
            double rainAmount = rain.getDouble(i);
            String recommendation;

            if (temp >= 38) {
                recommendation = "Stay indoors & keep cool";
            }
            else if (isHeavyRain(code, rainAmount)) {
                recommendation = "Stay indoors & avoid the rain";
            }
            else if (isRain(code, rainAmount)) {
                recommendation = "Take an umbrella";
            }
            else if (temp <= 10) {
                recommendation = "Bring a jacket & stay warm";
            }
            else if (temp >= 30) {
                recommendation = "Remember to hydrate";
            }
            else if (temp > 14 && temp < 30 && isGoodWeather(code)) {
                recommendation = "Go for a run & hang your laundry";
            }
            else if (temp > 14 && temp < 30 && isGoodWeather(code)) {
                recommendation = "Great weather to go outside";
            }

            else {
                recommendation = "Enjoy your day";
            }

            /*
             * Track continuous periods of good weather.
             */
            if (isGoodWeather(code) && temp > 14 && temp < 30) {
                if (!goodWeather) {
                    goodWeather = true;
                    goodWeatherStart = time.getString(i);
                }
            } else if (goodWeather) {
                goodWeather = false;
                goodWeatherEnd = time.getString(i);
            }

            temperatureArray[i] =
                    time.getString(i)
                            + "/"
                            + temperatures.getDouble(i)
                            + "/"
                            + rain.getDouble(i)
                            + "/"
                            + resolveCode(code)
                            + "/"
                            + recommendation;
        }
        return temperatureArray;
    }



    private boolean isGoodWeather(int weatherCode) {
        return weatherCode >= 0 && weatherCode <= 2;
    }

    private boolean isRain(int weatherCode, double rainAmount) {
        return (weatherCode >= 51 && weatherCode <= 57) || (weatherCode >= 61 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82)  || rainAmount > 0;
    }

    private boolean isHeavyRain(int weatherCode, double rainAmount) {
        return weatherCode == 65 || weatherCode == 67 || weatherCode == 82 || weatherCode >= 95 || rainAmount >= 5.0;
    }


    public String[] getTemperatures(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject hourly = jsonObject.getJSONObject("hourly");
        JSONArray time = hourly.getJSONArray("time");
        JSONArray WeatherCode = hourly.getJSONArray("weather_code");
        JSONArray rain = hourly.getJSONArray("rain");
        JSONArray temperatures = hourly.getJSONArray("temperature_2m");

        String[] temperatureArray = new String[temperatures.length()];

        for (int i = 0; i < temperatures.length(); i++) {
            temperatureArray[i] = time.getString(i) + "/" +  temperatures.getDouble(i) + "/" + rain.getInt(i) + "/" + resolveCode(WeatherCode.getInt(i));
        }

        return temperatureArray;
    }

    @FXML
    public ImageView currentWeatherImage;

    @FXML
    public Label currentWeatherText;

    /**
     * Initialises weather page
     */
    @FXML
    public void initialize() throws IOException {


        IPService ipService = new IPService();

        LocationData location = ipService.getIp();
        getWeather(location.latitude(), location.longitude());

    };


    /**
     * Navigates back to the Home page
     */
    @FXML
    private void onHome() {
        NavigationController.onHome(homeButton);
    }


    /**
     * Navigates to the Grocery Tab
     */
    @FXML
    private void onGroceries() {
        NavigationController.onGroceries(groceryButton);
    }

    /**
     * Navigates to the Weather page
     */
    @FXML
    private void onWeather(){
        NavigationController.onWeather(weatherButton);
    }

    /**
     * Navigates to the Electricity Tracker.
     */
    @FXML
    private void onUtilities() {
        NavigationController.onUtilities(utilityButton);
    }
}