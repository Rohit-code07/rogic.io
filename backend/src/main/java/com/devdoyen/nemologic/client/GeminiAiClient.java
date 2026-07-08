package com.devdoyen.nemologic.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!test")
public class GeminiAiClient implements AiClient {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.model.theme:gemini-2.5-flash}")
    private String themeModelName;

    @Value("${ai.model.grid:gemini-3.1-flash-lite}")
    private String gridModelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateDailyPuzzleJson() {
        return generatePuzzleJson(5, 5);
    }

    @Override
    public String generatePuzzleJson(int width, int height) {
        return generatePuzzleJson(width, height, java.util.Collections.emptyList());
    }

    @Override
    public String generatePuzzleJson(int width, int height, java.util.List<String> recentThemes) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("[AI] API Key is missing. Cannot generate AI puzzle.");
        }

        String excludePrompt = "";
        if (recentThemes != null && !recentThemes.isEmpty()) {
            excludePrompt = String.format("Do NOT generate puzzles with similar themes or names to the following: %s. ", String.join(", ", recentThemes));
        }

        int candidateCount = (width >= 25 || height >= 25) ? 2 : 5;
        String prompt = String.format(
            "Generate a JSON array of exactly %d different, creative, and recognizable pixel art grid designs of size %dx%d in JSON format. " +
            "Do NOT generate a simple heart shape. Create recognizable shapes. " +
            "%s" +
            "The response must follow this exact JSON schema (a JSON array of candidate objects): " +
            "[ { \"name\": \"ObjectName\", \"width\": %d, \"height\": %d, \"grid\": [[...], [...]] }, ... ]. " +
            "Do NOT prefix names with 'AI Puzzle:' or 'Daily Puzzle:'. Just output the pure name of the object. " +
            "Return only raw JSON string inside, no markdown formatting. " +
            "For each candidate, the 'grid' field MUST be a literal 2D JSON array representing %dx%d cells containing only 0 and 1. " +
            "Do NOT use any shorthand code, loops, functions, or placeholder syntax to define the grid. Every number MUST be explicitly outputted. " +
            "Ensure the filled cells form a recognizable connected shape with symmetry where appropriate, avoiding isolated noise pixels.",
            candidateCount, width, height, excludePrompt, width, height, width, height
        );

        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, gridModelName);
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] generatePuzzleJson attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw new RuntimeException("[AI] All attempts to generate puzzle JSON failed", lastException);
    }

    @Override
    public String generateThemeJson(int width, int height, java.util.List<String> recentThemes) {
        String prompt = String.format(
            "Generate a JSON array of exactly 10 different, highly creative, and recognizable pixel art theme concepts suitable for a %dx%d grid. " +
            "For each concept, provide a 'name' (a concise object/subject name, e.g. \"Espresso Cup\", \"Tiny Sailboat\") and " +
            "a 'description' (a short instruction explaining the pixel art details, including silhouette shape, key features, and visual patterns to represent on a %dx%d grid). " +
            "Do NOT generate themes or names similar to the following: %s. " +
            "Guidelines: pick unique subjects from diverse categories (e.g. food/beverage, everyday tools, plants/nature, vehicles, household items, animals, space, clothing). " +
            "The response must follow this exact JSON schema: [ { \"name\": \"ThemeName\", \"description\": \"Detailed rendering instruction...\" }, ... ]. " +
            "Do NOT wrap the response in markdown blocks. Output only raw JSON string.",
            width, height, width, height,
            (recentThemes == null || recentThemes.isEmpty()) ? "none" : String.join(", ", recentThemes)
        );

        int maxAttempts = 3;
        Exception lastException = null;

        // Try primary theme model (themeModelName)
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, themeModelName);
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                lastException = e;
                System.err.println("[AI] generateThemeJson with " + themeModelName + " attempt " + attempt + " failed (HTTP " + e.getStatusCode() + ")");
                if (e.getStatusCode().value() == 429) {
                    System.err.println("[AI] 429 Resource Exhausted. Falling back to " + gridModelName);
                    break; // Fallback immediately
                }
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] generateThemeJson with " + themeModelName + " attempt " + attempt + " failed: " + e.getMessage());
            }
            if (attempt < maxAttempts) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

        // Fallback to grid model (gemini-3.1-flash-lite)
        System.out.println("[AI] Calling fallback model " + gridModelName + " for theme generation...");
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, gridModelName);
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] Fallback theme generation attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new RuntimeException("[AI] All attempts to generate themes failed.", lastException);
    }

    @Override
    public String generatePuzzleJsonForTheme(int width, int height, String themeName, String themeDescription) {
        String fewShotExamples = 
            "Example of a valid 5x5 Nonogram design:\n" +
            "Theme: Apple\n" +
            "Description: A tiny apple shape with a stem at the top.\n" +
            "Grid:\n" +
            "[[0,0,1,0,0],\n" +
            " [0,1,1,1,0],\n" +
            " [1,1,1,1,1],\n" +
            " [1,1,1,1,1],\n" +
            " [0,1,1,1,0]]\n\n" +
            "Example of a valid 10x10 Nonogram design:\n" +
            "Theme: Sailboat\n" +
            "Description: A simple sailboat on the sea.\n" +
            "Grid:\n" +
            "[[0,0,0,0,1,0,0,0,0,0],\n" +
            " [0,0,0,1,1,0,0,0,0,0],\n" +
            " [0,0,1,1,1,0,0,0,0,0],\n" +
            " [0,1,1,1,1,0,0,0,0,0],\n" +
            " [1,1,1,1,1,1,0,0,0,0],\n" +
            " [0,0,0,0,1,0,0,0,0,0],\n" +
            " [0,1,1,1,1,1,1,1,1,0],\n" +
            " [0,0,1,1,1,1,1,1,0,0],\n" +
            " [0,0,0,0,0,0,0,0,0,0],\n" +
            " [0,0,0,0,0,0,0,0,0,0]]\n\n";

        int candidateCount = (width >= 25 || height >= 25) ? 2 : 5;
        String prompt = String.format(
            fewShotExamples +
            "Generate a JSON array of exactly %d candidate pixel art grid designs of size %dx%d for the theme '%s' in JSON format. " +
            "Theme Description: %s\n" +
            "The response must follow this exact JSON schema:\n" +
            "[ { \"name\": \"ObjectName\", \"width\": %d, \"height\": %d, \"grid\": [[...], [...]] }, ... ].\n" +
            "Do NOT prefix names with 'AI Puzzle:' or 'Daily Puzzle:'. Just use the name '%s' (or a minor creative variation like 'Mini %s' / 'Giant %s' depending on size).\n" +
            "Return only raw JSON string inside, no markdown formatting.\n" +
            "For each candidate, the 'grid' field MUST be a literal 2D JSON array representing %dx%d cells containing only 0 and 1.\n" +
            "Ensure the filled cells form a recognizable connected shape representing the theme description. " +
            "Do NOT use any shorthand code, loops, functions, or placeholder syntax to define the grid. Every number MUST be explicitly outputted.",
            candidateCount, width, height, themeName, themeDescription, width, height, themeName, themeName, themeName, width, height
        );

        int maxAttempts = 3;
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callGeminiApi(prompt, gridModelName);
            } catch (Exception e) {
                lastException = e;
                System.err.println("[AI] generatePuzzleJsonForTheme with " + gridModelName + " attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new RuntimeException("[AI] All attempts to generate grid for theme failed.", lastException);
    }

    private String callGeminiApi(String prompt, String model) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("[AI] API Key is missing. Cannot query Gemini API.");
        }

        String url = "https://generativelanguage.googleapis.com/v1/models/" + model + ":generateContent?key=" + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        contents.put("parts", Collections.singletonList(parts));
        requestBody.put("contents", Collections.singletonList(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String response = restTemplate.postForObject(url, entity, String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            String rawText = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            rawText = rawText.trim();
            if (rawText.startsWith("```json")) {
                rawText = rawText.substring(7);
            }
            if (rawText.startsWith("```")) {
                rawText = rawText.substring(3);
            }
            if (rawText.endsWith("```")) {
                rawText = rawText.substring(0, rawText.length() - 3);
            }
            return rawText.trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + response, e);
        }
    }
}
