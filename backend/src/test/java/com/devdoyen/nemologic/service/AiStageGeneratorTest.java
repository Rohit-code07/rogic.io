package com.devdoyen.nemologic.service;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.repository.StageRepository;
import com.devdoyen.nemologic.repository.ThemePoolRepository;
import com.devdoyen.nemologic.scheduler.DailyPuzzleScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AiStageGeneratorTest {

    private AiClient aiClient;
    private StageRepository stageRepository;
    private ThemePoolRepository themePoolRepository;
    private AiStageGenerator aiStageGenerator;
    private NonogramSolver nonogramSolver;

    @BeforeEach
    public void setUp() {
        aiClient = mock(AiClient.class);
        stageRepository = mock(StageRepository.class);
        themePoolRepository = mock(ThemePoolRepository.class);
        nonogramSolver = new NonogramSolver();
        aiStageGenerator = new AiStageGenerator(aiClient, stageRepository, nonogramSolver, themePoolRepository);

        when(stageRepository.findTop10ByOrderByIdDesc()).thenReturn(java.util.Collections.emptyList());
        when(stageRepository.findTop50ByOrderByIdDesc()).thenReturn(java.util.Collections.emptyList());
        when(stageRepository.existsBySolutionGrid(any())).thenReturn(false);
        when(stageRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
    }

    @Test
    public void testParseJsonAndSaveStage() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"AI Puzzle\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage();

        assertNotNull(stage);
        assertEquals("Puzzle", stage.getName());
        assertEquals(5, stage.getWidth());
        assertEquals(5, stage.getHeight());
        assertEquals("V3", stage.getGeneratorVersion());
        assertNotNull(stage.getSolutionGrid());
        assertEquals(1, stage.getSolutionGrid()[0][1]);
    }

    @Test
    public void testDailyPuzzleSchedulerTriggerChain() {
        AiStageGenerator mockGenerator = mock(AiStageGenerator.class);
        StageService mockStageService = mock(StageService.class);
        DailyPuzzleScheduler scheduler = new DailyPuzzleScheduler(mockGenerator, mockStageService);

        scheduler.generateDailyPuzzle();

        int[] sizes = {5, 10, 15, 20, 25, 30};
        for (int size : sizes) {
            verify(mockGenerator, times(5)).generateAndSaveStage(size, size, false);
        }
    }

    @Test
    public void testDailyPuzzleSchedulerReleaseChain() {
        AiStageGenerator mockGenerator = mock(AiStageGenerator.class);
        StageService mockStageService = mock(StageService.class);
        DailyPuzzleScheduler scheduler = new DailyPuzzleScheduler(mockGenerator, mockStageService);

        scheduler.releaseDailyPuzzle();

        verify(mockStageService, times(1)).releaseDailyPuzzles();
    }

    @Test
    public void testGenerateAndSaveStageRetriesOnFailureAndEventuallyThrows() {
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"Test\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString())).thenReturn("invalid json");

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(5)).generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString());
    }

    @Test
    public void testGenerateAndSaveStageRetriesOnNonUniqueSolution() {
        String mockNonUniqueJsonResponse = "{\"name\": \"Invalid AI Puzzle\", \"width\": 2, \"height\": 2, \"grid\": [[1,0],[0,1]]}";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"Invalid AI Puzzle\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockNonUniqueJsonResponse);

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage(2, 2, true);
        });

        verify(aiClient, times(5)).generatePuzzleJsonForTheme(eq(2), eq(2), anyString(), anyString());
    }

    @Test
    public void testGenerateAndSaveStageRetriesWhenClientThrowsException() {
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"Test\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString())).thenThrow(new RuntimeException("API error"));

        assertThrows(IllegalArgumentException.class, () -> {
            aiStageGenerator.generateAndSaveStage();
        });

        verify(aiClient, times(5)).generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString());
    }

    @Test
    public void testTitleCleaning() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle: Fantastic Tree\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"AI Puzzle: Fantastic Tree\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        assertEquals("Fantastic Tree", stage.getName());
    }

    @Test
    public void testCustomSizeGeneration() {
        // 3x3 grid (unique solution: all 1s)
        String mockJsonResponse = "{\"name\": \"Custom 3x3\", \"width\": 3, \"height\": 3, \"grid\": [[1,1,1],[1,1,1],[1,1,1]]}";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"Custom 3x3\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(eq(3), eq(3), anyString(), anyString())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(3, 3, true);

        assertNotNull(stage);
        assertEquals("Custom 3x3", stage.getName());
        assertEquals(3, stage.getWidth());
        assertEquals(3, stage.getHeight());
    }

    @Test
    public void testDuplicateGridDeduplication() {
        String mockJsonResponse = "{\"name\": \"AI Puzzle\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"AI Puzzle\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockJsonResponse);
        // First existsBySolutionGrid check returns true (duplicate), second returns false
        when(stageRepository.existsBySolutionGrid(any())).thenReturn(true).thenReturn(false);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        verify(aiClient, times(2)).generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString());
    }

    @Test
    public void testMultipleCandidatesSelectsLogicalOnly() {
        String mockJsonResponse = "[" +
            "{\"name\": \"NonUnique\", \"width\": 5, \"height\": 5, \"grid\": [[1,0,0,0,0],[0,1,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]}," +
            "{\"name\": \"Logical Heart\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}" +
            "]";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"Logical Heart\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        assertEquals("Logical Heart", stage.getName());
    }

    @Test
    public void testDuplicateNameDeduplication() {
        String mockJsonResponse = "{\"name\": \"Already Exists\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"Already Exists\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockJsonResponse);

        // Name exists on first check, then does not exist
        when(stageRepository.existsByNameIgnoreCase("Already Exists")).thenReturn(true).thenReturn(false);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);
        assertNotNull(stage);
        verify(aiClient, times(2)).generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString());
    }

    @Test
    public void testGridDensityValidation() {
        // Grid with density < 20% (1 filled cell out of 25 = 4%)
        String sparseJson = "{\"name\": \"Sparse\", \"width\": 5, \"height\": 5, \"grid\": [[1,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]}";
        // Grid with density > 80% (21 filled cells out of 25 = 84%)
        String denseJson = "{\"name\": \"Dense\", \"width\": 5, \"height\": 5, \"grid\": [[1,1,1,1,1],[1,1,1,1,1],[1,1,1,1,1],[1,1,1,1,1],[1,1,1,0,0]]}";
        // Normal grid (density 13 out of 25 = 52%)
        String normalJson = "{\"name\": \"Normal\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";

        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn("[{\"name\": \"Test\", \"description\": \"Desc\"}]");
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(sparseJson)
            .thenReturn(denseJson)
            .thenReturn(normalJson);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);
        assertNotNull(stage);
        assertEquals("Normal", stage.getName());
        verify(aiClient, times(3)).generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString());
    }

    @Test
    public void testThemeCacheFallbacks() {
        // If generateThemeJson throws error, it should use the static fallback list
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenThrow(new RuntimeException("API quota error"));
        
        String mockJsonResponse = "{\"name\": \"Apple 123\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
        when(aiClient.generatePuzzleJsonForTheme(anyInt(), anyInt(), anyString(), anyString())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);
        assertNotNull(stage);
        assertTrue(stage.getName().startsWith("Apple") || stage.getName().startsWith("Smile") || stage.getName().startsWith("Cup") || stage.getName().startsWith("Key") || stage.getName().startsWith("Star") || stage.getName().startsWith("Tree"));
    }

    @Test
    public void testGenerateAndSaveStageWithDbThemePool() {
        com.devdoyen.nemologic.model.ThemePool themeInPool = new com.devdoyen.nemologic.model.ThemePool("Db Guitar", "A beautiful acoustic guitar description", 5, 5);
        themeInPool.setId(42L);
        themeInPool.setUsed(false);

        when(themePoolRepository.findFirstUnused(eq(5), eq(5), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(java.util.Collections.singletonList(themeInPool));
        when(themePoolRepository.save(any(com.devdoyen.nemologic.model.ThemePool.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String mockJsonResponse = "{\"name\": \"Db Guitar\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
        when(aiClient.generatePuzzleJsonForTheme(eq(5), eq(5), eq("Db Guitar"), anyString())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        assertEquals("Db Guitar", stage.getName());
        assertTrue(themeInPool.isUsed());
        verify(themePoolRepository, times(1)).save(themeInPool);
    }

    @Test
    public void testGenerateAndSaveStageWithDbThemePoolExhaustedFallback() {
        when(themePoolRepository.findFirstUnused(eq(5), eq(5), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(java.util.Collections.emptyList());

        String mockJsonResponse = "{\"name\": \"Apple 123\", \"width\": 5, \"height\": 5, \"grid\": [[0,1,0,1,0],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0],[0,0,1,0,0]]}";
        when(aiClient.generatePuzzleJsonForTheme(eq(5), eq(5), anyString(), anyString())).thenReturn(mockJsonResponse);
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Stage stage = aiStageGenerator.generateAndSaveStage(5, 5, true);

        assertNotNull(stage);
        assertTrue(stage.getName().startsWith("Apple") || stage.getName().startsWith("Smile") || stage.getName().startsWith("Cup") 
            || stage.getName().startsWith("Key") || stage.getName().startsWith("Star") || stage.getName().startsWith("Tree"));
    }
}
