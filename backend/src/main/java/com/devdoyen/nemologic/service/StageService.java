package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class StageService {

    private final StageRepository stageRepository;

    public StageService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional(readOnly = true)
    public Page<Stage> getStagesPaged(int page, int size, Integer width) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        if (width != null) {
            return stageRepository.findByActiveAndApprovedAndWidth(true, true, width, pageable);
        }
        return stageRepository.findByActiveAndApproved(true, true, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Stage> getStagesForAdminPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return stageRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Stage> getAllStages() {
        return stageRepository.findByActiveAndApproved(true, true);
    }

    @Transactional(readOnly = true)
    public List<Stage> getAllStagesForAdmin() {
        return stageRepository.findAll();
    }

    @Transactional
    public void approveStage(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        stage.setApproved(true);
        stageRepository.save(stage);
    }

    @Transactional
    public void deleteStageSoft(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        stage.setActive(false);
        stageRepository.save(stage);
    }

    @Transactional
    public void restoreStage(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        stage.setActive(true);
        stage.setApproved(true);
        stageRepository.save(stage);
    }

    @Transactional
    public Stage saveStage(Stage stage) {
        return stageRepository.save(stage);
    }

    @Transactional
    public Stage likeStage(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        stage.setUpvotes(stage.getUpvotes() + 1);
        return stageRepository.save(stage);
    }

    @Transactional
    public Stage dislikeStage(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        stage.setDownvotes(stage.getDownvotes() + 1);
        return stageRepository.save(stage);
    }

    @Transactional
    public void activateAllInactiveStages() {
        List<Stage> inactive = stageRepository.findByActive(false);
        for (Stage stage : inactive) {
            stage.setActive(true);
            stageRepository.save(stage);
        }
    }

    @Transactional
    public void releaseDailyPuzzles() {
        int[] sizes = {5, 10, 15, 20, 25, 30};
        for (int size : sizes) {
            List<Stage> candidates = stageRepository.findByWidthAndHeightAndActiveAndApprovedOrderByIdAsc(size, size, false, true);
            if (!candidates.isEmpty()) {
                Stage oldest = candidates.get(0);
                oldest.setActive(true);
                stageRepository.save(oldest);
            }
        }
    }

    @Transactional(readOnly = true)
    public long getInactiveApprovedCount(int width, int height) {
        return stageRepository.countByWidthAndHeightAndActiveAndApproved(width, height, false, true);
    }

    @Transactional(readOnly = true)
    public Optional<Stage> getStageById(Long id) {
        return stageRepository.findById(id);
    }

    @Transactional
    public void startStage(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        stage.setTotalAttempts(stage.getTotalAttempts() + 1);
        stageRepository.save(stage);
    }

    @Transactional
    public void recordClear(Long id, int elapsedTime) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found: " + id));
        
        int currentClears = stage.getTotalClears();
        double currentAvg = stage.getAverageElapsedTime();
        
        double newAvg;
        if (currentClears == 0) {
            newAvg = elapsedTime;
        } else {
            newAvg = (currentAvg * currentClears + elapsedTime) / (currentClears + 1);
        }
        
        stage.setTotalClears(currentClears + 1);
        stage.setAverageElapsedTime(newAvg);
        stageRepository.save(stage);
    }

    @Transactional
    public void deactivateLowestFeedbackStage() {
        List<Stage> candidates = stageRepository.findLowestFeedbackStages();
        if (!candidates.isEmpty()) {
            Stage target = candidates.get(0);
            target.setActive(false);
            stageRepository.save(target);
        }
    }
}
