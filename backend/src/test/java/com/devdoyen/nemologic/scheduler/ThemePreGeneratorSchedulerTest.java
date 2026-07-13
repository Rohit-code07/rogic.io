package com.devdoyen.nemologic.scheduler;

import com.devdoyen.nemologic.client.AiClient;
import com.devdoyen.nemologic.model.ThemePool;
import com.devdoyen.nemologic.repository.ThemePoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ThemePreGeneratorSchedulerTest {

    private AiClient aiClient;
    private ThemePoolRepository themePoolRepository;
    private ThemePreGeneratorScheduler scheduler;

    @BeforeEach
    public void setUp() {
        aiClient = mock(AiClient.class);
        themePoolRepository = mock(ThemePoolRepository.class);
        scheduler = new ThemePreGeneratorScheduler(aiClient, themePoolRepository);
        // Turn off delays for testing speed
        scheduler.setDelays(0, 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPreGenerateThemesWhenPoolIsSufficient() {
        when(themePoolRepository.countByWidthAndHeightAndUsedFalse(anyInt(), anyInt())).thenReturn(25L);

        scheduler.preGenerateThemes();

        verify(aiClient, never()).generateThemeJson(anyInt(), anyInt(), anyList());
        verify(themePoolRepository, never()).save(any(ThemePool.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPreGenerateThemesWhenPoolIsLow() {
        when(themePoolRepository.countByWidthAndHeightAndUsedFalse(anyInt(), anyInt())).thenReturn(5L);

        String mockResponse = "[{\"name\":\"Theme A\",\"description\":\"Desc A\"},{\"name\":\"Theme B\",\"description\":\"Desc B\"}]";
        when(aiClient.generateThemeJson(anyInt(), anyInt(), anyList())).thenReturn(mockResponse);

        when(themePoolRepository.existsByNameIgnoreCase("Theme A")).thenReturn(false);
        when(themePoolRepository.existsByNameIgnoreCase("Theme B")).thenReturn(true);

        scheduler.preGenerateThemes();

        verify(aiClient, times(12)).generateThemeJson(anyInt(), anyInt(), anyList());

        ArgumentCaptor<ThemePool> captor = ArgumentCaptor.forClass(ThemePool.class);
        verify(themePoolRepository, times(12)).save(captor.capture());

        ThemePool savedTheme = captor.getValue();
        assertEquals("Theme A", savedTheme.getName());
        assertEquals("Desc A", savedTheme.getDescription());
    }
}
