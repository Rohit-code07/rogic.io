package com.devdoyen.nemologic.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class NemologicRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register static resources for puzzles
        hints.resources().registerPattern("puzzles/stages.json");
        // Register database migration scripts for Flyway
        hints.resources().registerPattern("db/migration/*.sql");

        // Register StageDto for reflection (needed for Jackson deserialization in native image)
        hints.reflection().registerType(
            com.devdoyen.nemologic.config.DataSeeder.StageDto.class,
            org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS,
            org.springframework.aot.hint.MemberCategory.DECLARED_FIELDS
        );

        // Register AiResponseDto for reflection (needed for Jackson deserialization in native image)
        hints.reflection().registerType(
            com.devdoyen.nemologic.service.AiStageGenerator.AiResponseDto.class,
            org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS,
            org.springframework.aot.hint.MemberCategory.DECLARED_FIELDS
        );

        // Register Nimbus JOSE/JWT types for native reflection (required for OAuth2 Resource Server)
        try {
            hints.reflection().registerType(
                Class.forName("com.nimbusds.jwt.SignedJWT"),
                org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS
            );
            hints.reflection().registerType(
                Class.forName("com.nimbusds.jose.JWSHeader"),
                org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS
            );
            hints.reflection().registerType(
                Class.forName("com.nimbusds.jose.util.JSONObjectUtils"),
                org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS
            );
        } catch (ClassNotFoundException e) {
            // Ignore if library is not on classpath in certain environments
        }
    }
}
