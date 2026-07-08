package com.devdoyen.nemologic.client;

public interface AiClient {
    String generateDailyPuzzleJson();
    String generatePuzzleJson(int width, int height);
    String generatePuzzleJson(int width, int height, java.util.List<String> recentThemes);
    String generateThemeJson(int width, int height, java.util.List<String> recentThemes);
    String generatePuzzleJsonForTheme(int width, int height, String themeName, String themeDescription);
}
