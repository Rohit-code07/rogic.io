package com.devdoyen.nemologic.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class MockAiClient implements AiClient {

    @Override
    public String generateDailyPuzzleJson() {
        return "{\"name\": \"AI Puzzle: Daily Apple\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
    }

    @Override
    public String generatePuzzleJson(int width, int height) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int fillRows = Math.max(1, height / 2);
        for (int r = 0; r < height; r++) {
            sb.append("[");
            for (int c = 0; c < width; c++) {
                sb.append(r < fillRows ? "1" : "0");
                if (c < width - 1) sb.append(",");
            }
            sb.append("]");
            if (r < height - 1) sb.append(",");
        }
        sb.append("]");
        String gridStr = sb.toString();
        return String.format("{\"name\": \"AI Puzzle: Custom Shape\", \"width\": %d, \"height\": %d, \"grid\": %s}", width, height, gridStr);
    }

    @Override
    public String generatePuzzleJson(int width, int height, java.util.List<String> recentThemes) {
        return generatePuzzleJson(width, height);
    }

    @Override
    public String generateThemeJson(int width, int height, java.util.List<String> recentThemes) {
        return "[" +
            "{\"name\":\"Mock Ramen Bowl\",\"description\":\"A detailed ramen bowl\"}," +
            "{\"name\":\"Mock Retro Cassette\",\"description\":\"A retro tape cassette\"}," +
            "{\"name\":\"Mock Sailboat\",\"description\":\"A classic sailboat on water\"}," +
            "{\"name\":\"Mock Penguin\",\"description\":\"A cute penguin with scarf\"}," +
            "{\"name\":\"Mock Cactus\",\"description\":\"A green cactus in desert\"}," +
            "{\"name\":\"Mock Rocket\",\"description\":\"A rocket launching into space\"}," +
            "{\"name\":\"Mock Guitar\",\"description\":\"An acoustic guitar outline\"}," +
            "{\"name\":\"Mock Butterfly\",\"description\":\"A colorful butterfly shape\"}," +
            "{\"name\":\"Mock Teapot\",\"description\":\"A steaming teapot design\"}," +
            "{\"name\":\"Mock Castle\",\"description\":\"A medieval castle tower\"}" +
            "]";
    }

    @Override
    public String generatePuzzleJsonForTheme(int width, int height, String themeName, String themeDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int fillRows = Math.max(1, height / 2);
        for (int r = 0; r < height; r++) {
            sb.append("[");
            for (int c = 0; c < width; c++) {
                sb.append(r < fillRows ? "1" : "0");
                if (c < width - 1) sb.append(",");
            }
            sb.append("]");
            if (r < height - 1) sb.append(",");
        }
        sb.append("]");
        String gridStr = sb.toString();
        return String.format("[{\"name\": \"%s\", \"width\": %d, \"height\": %d, \"grid\": %s}]", themeName, width, height, gridStr);
    }
}
