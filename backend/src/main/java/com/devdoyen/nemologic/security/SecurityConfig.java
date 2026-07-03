package com.devdoyen.nemologic.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminAuthenticationFilter adminAuthenticationFilter;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    public SecurityConfig(AdminAuthenticationFilter adminAuthenticationFilter) {
        this.adminAuthenticationFilter = adminAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/auth/me").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/*/clear").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/history").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(org.springframework.security.config.Customizer.withDefaults())
                .bearerTokenResolver(bearerTokenResolver())
            )
            .addFilterBefore(adminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.security.oauth2.server.resource.web.BearerTokenResolver bearerTokenResolver() {
        org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver defaultResolver = 
            new org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver();
        return request -> {
            // Bypass resource server token resolution for admin endpoints so custom tokens aren't parsed as JWT
            if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/admin/")) {
                return null;
            }
            return defaultResolver.resolve(request);
        };
    }

    @Bean
    @org.springframework.context.annotation.Profile("!test")
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }

    @Bean
    @org.springframework.context.annotation.Profile("test")
    public JwtDecoder testJwtDecoder() {
        return token -> org.springframework.security.oauth2.jwt.Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "google-oauth-12345")
                .claim("name", "John Doe")
                .claim("email", "john@example.com")
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
