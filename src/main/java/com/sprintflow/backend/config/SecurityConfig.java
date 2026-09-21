package com.sprintflow.backend.config;

import com.sprintflow.backend.security.JwtAuthenticationFilter;
import com.sprintflow.backend.security.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oauth2SuccessHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OAuth2SuccessHandler oauth2SuccessHandler) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/ws/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception

                        // 401 - User is not authenticated
                        .authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.sendError(
                                            HttpServletResponse.SC_UNAUTHORIZED,
                                            "Authentication required"
                                    );
                                }
                        )

                        // 403 - User is authenticated but not allowed
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                    response.sendError(
                                            HttpServletResponse.SC_FORBIDDEN,
                                            "Access denied"
                                    );
                                }
                        )
                )

                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oauth2SuccessHandler)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}