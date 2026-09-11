package com.example.crawler.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class AIService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askQuestion(String knowledge, String question) throws Exception {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException(
                    "GEMINI_API_KEY environment variable is not set.");
        }

        String prompt = """
                You are a website question-answering assistant.

                Answer the user's question using ONLY the information
                provided in the website knowledge below.

                If the answer cannot be found in the website knowledge,
                clearly say that the information was not found.

                WEBSITE KNOWLEDGE:
                %s

                USER QUESTION:
                %s
                """.formatted(knowledge, question);

        Map<String, Object> requestData = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        String requestBody =
                objectMapper.writeValueAsString(requestData);

        HttpClient client = HttpClient.newHttpClient();

        String endpoint =
                apiUrl + "/" + model + ":generateContent";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new RuntimeException(
                    "Gemini API error: " + response.body());
        }

        JsonNode jsonResponse =
                objectMapper.readTree(response.body());

        JsonNode candidates =
                jsonResponse.path("candidates");

        if (candidates.isArray() && candidates.size() > 0) {

            JsonNode text =
                    candidates.get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text");

            if (!text.isMissingNode()) {
                return text.asText();
            }
        }

        throw new RuntimeException(
                "Could not find AI answer in Gemini response.");
    }
}
