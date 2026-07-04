package com.devdoyen.nemologic.config;

import com.devdoyen.nemologic.dto.HistoryResponse;
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
}
