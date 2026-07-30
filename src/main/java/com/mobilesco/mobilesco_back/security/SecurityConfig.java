package com.mobilesco.mobilesco_back.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${app.openapi.public-access:false}")
    private boolean openApiPublicAccess;

    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(
        "http://localhost",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://localhost:80",
        "https://mobilesco.cloud",
        "https://www.mobilesco.cloud",
        "https://erp.mobilesco.cloud",
        // Web publica que consume el catalogo publico (/api/v1/public/**)
        "https://mobilesco.com.mx",
        "https://www.mobilesco.com.mx"
    ));

    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
}

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {

    http
        .cors(cors -> {})
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> {
            auth.requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register").permitAll();
            if (openApiPublicAccess) {
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
            } else {
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .hasAnyRole("ADMIN", "SUPER_ADMIN");
            }
            auth
                // Solo los recursos que forman parte del catalogo son publicos.
                .requestMatchers(HttpMethod.GET,
                        "/uploads/modelos/**",
                        "/uploads/productos/catalogo/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                // Las demas cargas, incluidas fotos de empleados, requieren autenticacion.
                .requestMatchers("/uploads/**").authenticated()
                .anyRequest().authenticated();
        })
        // 👇 Aquí agregamos el filtro JWT
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

    // Esto nos permite usar AuthenticationManager en nuestro controller/service
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
}
