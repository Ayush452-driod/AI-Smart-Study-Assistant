
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
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public GeminiService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            ObjectMapper mapper) {

        this.apiKey = apiKey;
        this.model = model;
        this.mapper = mapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public String ask(String prompt) {

        if (apiKey == null ||
                apiKey.isBlank() ||
                apiKey.startsWith("YOUR_")) {

            throw new RuntimeException(
                    "AI is not configured. Add your Gemini API key in application.properties."
            );
        }

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model
                        + ":generateContent";

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        try {

            String json = mapper.writeValueAsString(body);

            // Retry temporary Gemini errors up to 3 times
            int maxAttempts = 3;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(60))
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                int status = response.statusCode();

                System.out.println(
                        "Gemini HTTP Status: " + status
                                + " | Attempt: " + attempt
                );

                // Successful response
                if (status >= 200 && status < 300) {

                    return extractText(response.body());
                }

                // Temporary errors that can be retried
                boolean retryable =
                        status == 429 ||
                        status == 500 ||
                        status == 502 ||
                        status == 503 ||
                        status == 504;

                String errorMessage = getErrorMessage(response.body());

                if (retryable && attempt < maxAttempts) {

                    long delay = attempt * 2000L;

                    System.out.println(
                            "Gemini temporarily unavailable. Retrying in "
                                    + delay + " ms..."
                    );

                    Thread.sleep(delay);

                    continue;
                }

                throw new RuntimeException(
                        "Gemini API Error (" + status + "): " + errorMessage
                );
            }

            throw new RuntimeException(
                    "Gemini is temporarily unavailable. Please try again."
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Gemini request was interrupted. Please try again."
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not connect to Gemini: " + e.getMessage(),
                    e
            );
        }
    }


    // Extract AI-generated text
    private String extractText(String responseBody) {

        try {

            JsonNode root = mapper.readTree(responseBody);

            JsonNode text = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (text.isMissingNode() || text.asText().isBlank()) {

                String finishReason = root.path("candidates")
                        .path(0)
                        .path("finishReason")
                        .asText("");

                throw new RuntimeException(
                        "Gemini returned an empty response. "
                                + (finishReason.isBlank()
                                ? ""
                                : "Finish reason: " + finishReason)
                );
            }

            return text.asText();

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to read Gemini response.",
                    e
            );
        }
    }


    // Extract useful error message from Gemini
    private String getErrorMessage(String responseBody) {

        try {

            JsonNode root = mapper.readTree(responseBody);

            String message = root.path("error")
                    .path("message")
                    .asText("");

            if (!message.isBlank()) {
                return message;
            }

        } catch (Exception ignored) {
            // Use fallback message below
        }

        return "Gemini service is temporarily unavailable.";
    }
}
