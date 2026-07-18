package com.devdoyen.nemologic.scheduler;

import com.devdoyen.nemologic.service.AiStageGenerator;
import com.devdoyen.nemologic.service.StageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyPuzzleScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyPuzzleScheduler.class);

    private final AiStageGenerator aiStageGenerator;
    private final StageService stageService;

    @Value("${app.scheduler.generate-sleep-ms:15000}")
    private long sleepIntervalMs;

    public DailyPuzzleScheduler(AiStageGenerator aiStageGenerator, StageService stageService) {
        this.aiStageGenerator = aiStageGenerator;
        this.stageService = stageService;
    }

    @Scheduled(cron = "0 17 4 * * ?")
    public void generateDailyPuzzle() {
        int[] sizes = {5, 10, 15, 20, 25, 30};
        for (int size : sizes) {
            try {
                long currentBuffer = stageService.getInactiveApprovedCount(size, size);
                long targetBuffer = 5;
                if (currentBuffer < targetBuffer) {
                    long needed = targetBuffer - currentBuffer;
                    log.info("[Scheduler] Size {}x{} pool size is {}. Refilling {} puzzles...", size, size, currentBuffer, needed);
                    for (int i = 0; i < needed; i++) {
                        aiStageGenerator.generateAndSaveStage(size, size, false);
                        try {
                            Thread.sleep(sleepIntervalMs); // delay to prevent Gemini API rate limiting (5 RPM)
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    log.info("[Scheduler] Size {}x{} pool size is {} (sufficient).", size, size, currentBuffer);
                }
            } catch (Exception e) {
                log.error("[Scheduler] Failed to verify/refill daily puzzle of size {}x{}: {}", size, size, e.getMessage(), e);
            }
            try {
                Thread.sleep(sleepIntervalMs); // delay between sizes
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Scheduled(cron = "${app.daily-puzzle.cron}")
    public void releaseDailyPuzzle() {
        stageService.releaseDailyPuzzles();
    }

    @Scheduled(cron = "${app.daily-cleanup.cron}")
    public void cleanupLowestFeedbackPuzzle() {
        log.info("[Scheduler] Starting puzzle cleanup based on lowest negative feedback...");
        try {
            stageService.deactivateLowestFeedbackStage();
        } catch (Exception e) {
            log.error("[Scheduler] Failed to cleanup lowest feedback stage: {}", e.getMessage(), e);
        }
    }
}
