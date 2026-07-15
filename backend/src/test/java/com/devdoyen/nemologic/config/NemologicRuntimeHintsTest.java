package com.devdoyen.nemologic.config;

import com.devdoyen.nemologic.dto.HistoryResponse;
import com.devdoyen.nemologic.dto.TelemetryStatsResponse;
import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import static org.assertj.core.api.Assertions.assertThat;

class NemologicRuntimeHintsTest {

    @Test
    void shouldRegisterHistoryResponseForReflection() {
        RuntimeHints hints = new RuntimeHints();
        NemologicRuntimeHints registrar = new NemologicRuntimeHints();
        registrar.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(HistoryResponse.class)).accepts(hints);
    }

    @Test
    void shouldRegisterTelemetryStatsResponseForReflection() {
        RuntimeHints hints = new RuntimeHints();
        NemologicRuntimeHints registrar = new NemologicRuntimeHints();
        registrar.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(TelemetryStatsResponse.class)).accepts(hints);
    }

    @Test
    void shouldRegisterUserModelForReflection() {
        RuntimeHints hints = new RuntimeHints();
        NemologicRuntimeHints registrar = new NemologicRuntimeHints();
        registrar.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(User.class)).accepts(hints);
    }

    @Test
    void shouldRegisterStageModelForReflection() {
        RuntimeHints hints = new RuntimeHints();
        NemologicRuntimeHints registrar = new NemologicRuntimeHints();
        registrar.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(Stage.class)).accepts(hints);
    }

    @Test
    void shouldRegisterResources() {
        RuntimeHints hints = new RuntimeHints();
        NemologicRuntimeHints registrar = new NemologicRuntimeHints();
        registrar.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource("puzzles/stages.json")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("db/migration/V1__init.sql")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("prompts/theme-generation.txt")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("prompts/grid-generation.txt")).accepts(hints);
    }
}
