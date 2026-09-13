package com.example.lifestylelevellingasianlifestyle;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class IPService {

    public String getIp() throws IOException {

        String urlString = String.format(
                "https://geoapi.info/api/geo"
        );

        try {
            URL url = URI.create(urlString).toURL();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setConnectTimeout(2500);
            con.setReadTimeout(2500);

            int responseCode = con.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("IP Service API returned: " + responseCode);
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
            JSONObject location = jsonObject.getJSONObject("location");
            String cityName = location.getString("city");
            JSONObject coordinates = location.getJSONObject("coordinates");
            double Colat = coordinates.getDouble("latitude");
            double Colong= coordinates.getDouble("longitude");
            System.out.println("User is in " + cityName + " with the coordinates of " +
                            Colat + " latatude & " + Colong + " longitude!");

            con.disconnect();

            return "1";

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

