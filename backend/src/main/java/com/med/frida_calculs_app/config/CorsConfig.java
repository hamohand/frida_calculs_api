package com.med.frida_calculs_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:4200,http://localhost:3000,http://localhost:8080}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.max-age:3600}")
    private Long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origins autorisées
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        log.info("CORS - Origins autorisées: {}", Arrays.toString(allowedOrigins));

        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        log.info("CORS - Méthodes autorisées: {}", Arrays.toString(allowedMethods));

        // Headers autorisés
        configuration.setAllowedHeaders(List.of(allowedHeaders.split(",")));

        // Autoriser les credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Temps de cache des preflight requests
        configuration.setMaxAge(maxAge);

        // Headers exposés au client
        configuration.setExposedHeaders(Arrays.asList(
            "X-Total-Count",
            "X-Request-Id",
            "Content-Disposition"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("Configuration CORS initialisée avec succès");
        return source;
    }
}
