package com.example.lifestylelevellingasianlifestyle;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Functionality for querying to an external AI service through an API and retrieving response
 */
public class AIService {
    /**
     * Sends a query to AI
     * @param message that is sent to external AI
     * @return the generated response
     * @throws IOException if error occurs while querying
     */
    public String queryAI(String message) throws IOException {

        String urlString = String.format(
                "https://openrouter.ai/api/v1/chat/completions"
        );

        try {
            URL url = URI.create(urlString).toURL();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            // AI token, it's the free plan so it's fine
            con.setRequestProperty("Authorization", "Bearer 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");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            // template stuff rn
            String jsonInputString = "{'model': 'nvidia/nemotron-3-ultra-550b-a55b:free','messages': [{'role': 'user', 'content': '"+ message +"'}]}";
            con.setConnectTimeout(2500);
            con.setReadTimeout(900000000);
            OutputStream os = con.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(jsonInputString);
            osw.flush();
            osw.close();
            os.close();  //don't forget to close the OutputStream
            con.connect();

            int responseCode = con.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("AI Service API returned: " + responseCode);
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
            JSONObject result = jsonObject.getJSONObject("result");
            JSONObject choices = result.getJSONObject("choices");
            JSONArray jsonArray = new JSONArray(choices);
            JSONObject firstObject = jsonArray.getJSONObject(0);
            String AIResponseOut = firstObject.getString("message");
            System.out.println("The AI says " + AIResponseOut);

            con.disconnect();
            return AIResponseOut;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

