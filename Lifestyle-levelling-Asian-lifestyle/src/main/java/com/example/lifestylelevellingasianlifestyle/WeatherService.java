package com.example.lifestylelevellingasianlifestyle;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class WeatherService {

    public String getWeather(double latitude, double longitude) {

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
                return null;
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
//            JSONArray jsonArray = new JSONArray(jsonBuffer.toString());
            JSONObject curently = jsonObject.getJSONObject("current");
            String simpleCurrentWeather = resolveCode(curently.getInt("weather_code"));
            boolean isDay = intToBooleanWithThrowing((curently.getInt("is_day")));
            int currentTemp = curently.getInt("temperature_2m");
            int currentWeatherCode = curently.getInt("weather_code");
//            JSONObject forecast = jsonObject.getJSONObject("coordinates");
//            double Colat = forecast.getDouble("latitude");
//            double Colong= forecast.getDouble("longitude");

            JSONObject hourly = jsonObject.getJSONObject("hourly");
            JSONArray times =hourly.getJSONArray("time");
            JSONArray temperatures = hourly.getJSONArray("temperature_2m");


            String[] temperaturess = getTemperatures(jsonBuffer.toString());

//            for (int i = 0; i < times.length(); i++) {
//                String hourTime = times.getString(i);
//                double hourTemp = temperatures.getDouble(i);
//                String timeOnly = hourTime.split("T")[1];
//                data.get().setTemperatureData(timeOnly,hourTemp);
//            }

            System.out.println("It is currently " + currentTemp + " degrees with " +
                    simpleCurrentWeather + "!");
            System.out.println(Arrays.toString(temperaturess));

            con.disconnect();
            return "-1";


//            System.out.println(response.toString());
//            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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


    boolean intToBooleanWithThrowing(int theInt) {
        if (theInt == 1) {
            return true;
        }
        if (theInt == 0) {
            return false;
        }
        throw new IllegalArgumentException("Invalid input, Only 0 or 1 is allowed.");
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


//    public class weatherCodes {
//        public String resolveCode(int code) {
//            String weatherType;
//            switch (code) {
//                case 0:
//                    weatherType = "Clear Sky";
//                    break;
//                case 1:
//                    weatherType = "Mainly Clear";
//                    break;
//                case 2:
//                    weatherType = "Partly Cloudy";
//                    break;
//                case 3:
//                    weatherType = "Overcast";
//                    break;
//                case 45:
//                    weatherType = "Fog";
//                    break;
//                case 48:
//                    weatherType = "Depositing Rime Fog";
//                    break;
//                case 51:
//                    weatherType = "Light Drizzle";
//                    break;
//                case 55:
//                    weatherType = "Intense Drizzle";
//                    break;
//                case 53:
//                    weatherType = "Moderate Drizzle";
//                    break;
//                case 56:
//                    weatherType = "Light Freezing Drizzle";
//                    break;
//                case 57:
//                    weatherType = "Dense Freezing Drizzle";
//                    break;
//                case 61:
//                    weatherType = "Slight Rain";
//                    break;
//                case 63:
//                    weatherType = "Moderate Rain";
//                    break;
//                case 65:
//                    weatherType = "Heavy Rain";
//                    break;
//                case 66:
//                    weatherType = "Light Freezing Rain";
//                    break;
//                case 67:
//                    weatherType = "Heavy Freezing Rain";
//                    break;
//                case 71:
//                    weatherType = "Slight SnowFall";
//                    break;
//                case 73:
//                    weatherType = "Moderate SnowFall";
//                    break;
//                case 75:
//                    weatherType = "Heavy SnowFall";
//                    break;
//                case 77:
//                    weatherType = "Snow Grains";
//                    break;
//                case 80:
//                    weatherType = "Slight Rain Showers";
//                    break;
//                case 81:
//                    weatherType = "Moderate Rain Showers";
//                    break;
//                case 82:
//                    weatherType = "Heavy Rain Showers";
//                    break;
//                case 85:
//                    weatherType = "Slight Snow Showers";
//                    break;
//                case 86:
//                    weatherType = "Heavy Snow Showers";
//                    break;
//                case 95:
//                    weatherType = "ThunderStorm";
//                    break;
//                case 96:
//                    weatherType = "ThunderStorm with Sligh Hail";
//                    break;
//                case 99:
//                    weatherType = "ThunderStorm with Heavy Hail";
//                    break;
//                default:
//                    System.out.println("Invalid Code");
//                    weatherType = "";
//            }
//            System.out.println(weatherType);
//            return weatherType;
//        }
//    }
}