package com.smartstudy.assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class GeminiService {

    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model) {

        this.apiKey = apiKey;
        this.model = model;
    }

    public String ask(String prompt) {

        if (apiKey == null ||
                apiKey.isBlank() ||
                apiKey.startsWith("YOUR_")) {

            return "AI is not configured. Add your Gemini API key in application.properties.";
        }

        try {

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model
                    + ":generateContent?key="
                    + apiKey;

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{
                            Map.of(
                                    "parts", new Object[]{
                                            Map.of("text", prompt)
                                    }
                            )
                    }
            );

            String json = mapper.writeValueAsString(body);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(60))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient()
                            .send(
                                    request,
                                    HttpResponse.BodyHandlers.ofString()
                            );

            System.out.println("Gemini HTTP Status: " + response.statusCode());
            System.out.println("Gemini Response: " + response.body());

            if (response.statusCode() / 100 != 2) {

                return "Gemini API Error (" +
                        response.statusCode() +
                        "): " +
                        response.body();
            }

            JsonNode root =
                    mapper.readTree(response.body());

            JsonNode text =
                    root.path("candidates")
                            .path(0)
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("text");

            if (text.isMissingNode() || text.asText().isBlank()) {

                return "AI returned an empty response.";
            }

            return text.asText();

        } catch (Exception e) {

            e.printStackTrace();

            return "Could not connect to the AI service: "
                    + e.getMessage();
        }
    }
}