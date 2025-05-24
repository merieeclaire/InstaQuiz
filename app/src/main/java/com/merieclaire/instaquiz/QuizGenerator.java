package com.merieclaire.instaquiz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuizGenerator {

    private static final String FUNCTION_URL = "https://us-central1-instaquiz-f33a4.cloudfunctions.net/generateQuiz";// Replace with your actual deployed URL

    public static List<String> generate(String content, Map<String, Integer> formatCounts) {
        List<String> quiz = new ArrayList<>();

        try {
            // Construct the JSON payload
            JSONObject payload = new JSONObject();
            payload.put("text", content);

            JSONObject formats = new JSONObject();
            for (Map.Entry<String, Integer> entry : formatCounts.entrySet()) {
                formats.put(entry.getKey(), entry.getValue());
            }
            payload.put("quizFormatCounts", formats);

            // Open connection
            URL url = new URL(FUNCTION_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Send the payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject responseJson = new JSONObject(response.toString());
                JSONArray quizArray = responseJson.getJSONArray("quiz");

                for (int i = 0; i < quizArray.length(); i++) {
                    quiz.add(quizArray.getString(i));
                }
            } else {
                Log.e("QuizGenerator", "Quiz generation failed. HTTP Code: " + responseCode);
            }
        } catch (Exception e) {
            Log.e("QuizGenerator", "Error generating quiz: ", e);
        }

        return quiz;
    }
}
