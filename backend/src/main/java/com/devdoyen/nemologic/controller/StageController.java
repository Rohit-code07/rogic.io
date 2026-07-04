package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.service.StageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/stages")
public class StageController {

    private final StageService stageService;
    private final com.devdoyen.nemologic.service.AiStageGenerator aiStageGenerator;

    public StageController(StageService stageService, com.devdoyen.nemologic.service.AiStageGenerator aiStageGenerator) {
        this.stageService = stageService;
        this.aiStageGenerator = aiStageGenerator;
    }

    @GetMapping
    public ResponseEntity<?> getAllStages(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer page,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer width,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer height) {
        if (page == null && size == null) {
            List<Stage> stages = stageService.getAllStages();
            if (stages.size() > 100) {
                stages = stages.subList(0, 100);
            }
            return ResponseEntity.ok(stages);
        }

        int pageVal = (page != null) ? Math.max(0, page) : 0;
        int sizeVal = (size != null) ? Math.min(100, Math.max(1, size)) : 20;

        return ResponseEntity.ok(stageService.getStagesPaged(pageVal, sizeVal, width));
    }

    @org.springframework.beans.factory.annotation.Value("${app.daily-puzzle.cron}")
    private String dailyPuzzleCron;

    @GetMapping("/next-release-delay")
    public ResponseEntity<Long> getNextReleaseDelaySeconds() {
        org.springframework.scheduling.support.CronExpression cron = 
            org.springframework.scheduling.support.CronExpression.parse(dailyPuzzleCron);
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        java.time.ZonedDateTime nextExecution = cron.next(now);
        if (nextExecution == null) {
            return ResponseEntity.ok(0L);
        }
        long delaySeconds = java.time.Duration.between(now, nextExecution).getSeconds();
        return ResponseEntity.ok(delaySeconds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stage> getStageById(@PathVariable Long id) {
        return stageService.getStageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/start")
    public ResponseEntity<Void> startStage(@PathVariable Long id) {
        try {
            stageService.startStage(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/ai-generate")
    public ResponseEntity<Stage> triggerAiGeneration() {
        try {
            Stage generated = aiStageGenerator.generateAndSaveStage();
            return ResponseEntity.ok(generated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/like")
    public ResponseEntity<Stage> likeStage(@PathVariable Long id) {
        try {
            Stage updated = stageService.likeStage(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/dislike")
    public ResponseEntity<Stage> dislikeStage(@PathVariable Long id) {
        try {
            Stage updated = stageService.dislikeStage(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
