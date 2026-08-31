package com.example.lifestylelevellingasianlifestyle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

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
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int responseCode = con.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Weather API returned: " + responseCode);
                return null;
            }

            StringBuilder response = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            con.disconnect();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}