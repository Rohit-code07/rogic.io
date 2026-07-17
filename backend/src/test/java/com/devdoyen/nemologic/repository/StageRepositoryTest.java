package com.devdoyen.nemologic.repository;

import com.devdoyen.nemologic.model.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public class StageRepositoryTest {

    @Autowired
    private StageRepository stageRepository;

    @Test
    public void testSaveAndFindStage() {
        int[][] grid = {
            {0, 1, 0},
            {1, 1, 1},
            {0, 1, 0}
        };
        Stage stage = new Stage(null, "TestStage", 3, 3, grid);
        Stage savedStage = stageRepository.save(stage);

        assertNotNull(savedStage.getId());

        Stage foundStage = stageRepository.findById(savedStage.getId()).orElse(null);
        assertNotNull(foundStage);
        // Green Phase: Assert actual value
        assertEquals("TestStage", foundStage.getName());
    }

    @Test
    public void testFindLowestFeedbackStages() {
        int[][] grid = {{1}};
        
        // s1: active=true, score: 2 - 5 = -3
        Stage s1 = new Stage(null, "Stage1", 1, 1, grid);
        s1.setActive(true);
        s1.setUpvotes(2);
        s1.setDownvotes(5);
        stageRepository.save(s1);

        // s2: active=true, score: 3 - 8 = -5 (lowest)
        Stage s2 = new Stage(null, "Stage2", 1, 1, grid);
        s2.setActive(true);
        s2.setUpvotes(3);
        s2.setDownvotes(8);
        stageRepository.save(s2);

        // s3: active=true, score: 5 - 2 = +3 (positive, should be ignored)
        Stage s3 = new Stage(null, "Stage3", 1, 1, grid);
        s3.setActive(true);
        s3.setUpvotes(5);
        s3.setDownvotes(2);
        stageRepository.save(s3);

        // s4: active=false, score: 0 - 10 = -10 (inactive, should be ignored)
        Stage s4 = new Stage(null, "Stage4", 1, 1, grid);
        s4.setActive(false);
        s4.setUpvotes(0);
        s4.setDownvotes(10);
        stageRepository.save(s4);

        java.util.List<Stage> result = stageRepository.findLowestFeedbackStages();

        assertEquals(2, result.size());
        assertEquals("Stage2", result.get(0).getName()); // score -5 is lowest
        assertEquals("Stage1", result.get(1).getName()); // score -3 is next lowest
    }
}
