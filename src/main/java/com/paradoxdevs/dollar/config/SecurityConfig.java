package com.paradoxdevs.dollar.config;

import com.paradoxdevs.dollar.filter.JwtAuthEntryPoint;
import com.paradoxdevs.dollar.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.paradoxdevs.dollar.constant.AppConstants.AUTH_ROUTES;
import static com.paradoxdevs.dollar.constant.AppConstants.CHANGE_PASSWORD_ROUTE;
import static com.paradoxdevs.dollar.constant.AppConstants.H2_CONSOLE;
import static com.paradoxdevs.dollar.constant.AppConstants.HEALTH_ROUTE;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthEntryPoint unauthorizedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CHANGE_PASSWORD_ROUTE).authenticated()
                        .requestMatchers(AUTH_ROUTES).permitAll()
                        .requestMatchers(HEALTH_ROUTE).permitAll()
                        .requestMatchers(H2_CONSOLE).permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
