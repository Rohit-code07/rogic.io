package com.devdoyen.nemologic.scheduler;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.ThemePool;
import com.devdoyen.nemologic.repository.ThemePoolRepository;
import com.devdoyen.nemologic.service.AiStageGenerator.ThemeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ThemePreGeneratorScheduler {

    private static final Logger log = LoggerFactory.getLogger(ThemePreGeneratorScheduler.class);

    private final AiClient aiClient;
    private final ThemePoolRepository themePoolRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private long apiDelayMs = 13000;
    private long sizeDelayMs = 5000;

    public ThemePreGeneratorScheduler(AiClient aiClient, ThemePoolRepository themePoolRepository) {
        this.aiClient = aiClient;
        this.themePoolRepository = themePoolRepository;
    }

    public void setDelays(long apiDelayMs, long sizeDelayMs) {
        this.apiDelayMs = apiDelayMs;
        this.sizeDelayMs = sizeDelayMs;
    }

    @Scheduled(cron = "${app.theme-pre-generator.cron:0 0 */2 * * ?}")
    public void preGenerateThemes() {
        int[] sizes = {5, 10, 15, 20, 25, 30};
        int targetBuffer = 20;

        for (int size : sizes) {
            try {
                long currentBuffer = themePoolRepository.countByWidthAndHeightAndUsedFalse(size, size);
                if (currentBuffer < targetBuffer) {
                    long needed = targetBuffer - currentBuffer;
                    log.info("[ThemePreGenerator] Size {}x{} unused count is {}. Refilling (target: {})...", size, size, currentBuffer, targetBuffer);

                    int iterations = (int) Math.ceil((double) needed / 10);
                    for (int i = 0; i < iterations; i++) {
                        List<String> recentThemes = new ArrayList<>();

                        String themeJson = aiClient.generateThemeJson(size, size, recentThemes);
                        if (themeJson != null && !themeJson.isEmpty()) {
                            com.fasterxml.jackson.core.type.TypeReference<List<ThemeDto>> typeRef =
                                new com.fasterxml.jackson.core.type.TypeReference<List<ThemeDto>>() {};
                            List<ThemeDto> generated = objectMapper.readValue(themeJson.trim(), typeRef);

                            if (generated != null) {
                                for (ThemeDto t : generated) {
                                    if (t.getName() == null || t.getName().trim().isEmpty()) {
                                        continue;
                                    }
                                    String name = t.getName().trim();
                                    if (!themePoolRepository.existsByNameIgnoreCase(name)) {
                                        ThemePool newTheme = new ThemePool(name, t.getDescription(), size, size);
                                        themePoolRepository.save(newTheme);
                                    }
                                }
                            }
                        }

                        // Wait to protect Gemini 2.5 Flash API rate limit (5 RPM)
                        try {
                            if (apiDelayMs > 0) {
                                Thread.sleep(apiDelayMs);
                            }
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } else {
                    log.info("[ThemePreGenerator] Size {}x{} unused count is {} (sufficient).", size, size, currentBuffer);
                }
            } catch (Exception e) {
                log.error("[ThemePreGenerator] Failed to pre-generate themes for size {}x{}: {}", size, size, e.getMessage(), e);
            }

            // Extra safety delay between sizes
            try {
                if (sizeDelayMs > 0) {
                    Thread.sleep(sizeDelayMs);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
