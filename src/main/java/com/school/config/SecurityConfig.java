package com.school.config;

import com.school.security.service.JwtAuthFilter;
import com.school.security.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(PasswordEncoder passwordEncoder,
                          UserService userService,
                          JwtAuthFilter jwtAuthFilter) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtAuthFilter = jwtAuthFilter;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers(
                                "/login",
                                "/complete-registration",
                                "/stk/callback/**"
                        ).permitAll()

                        // admin-only
                        .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")

                        // authenticated user profile
                        .requestMatchers("/me", "/auth/me").authenticated()

                        // shared school data
                        .requestMatchers(
                                "/classes/**",
                                "/subjects/**",
                                "/terms/**",
                                "/assignments/**",
                                "/teachers/**",
                                "/students/**",
                                "/parents/**"
                        ).hasAnyRole("SUPER_ADMIN", "ADMIN", "STAFF", "TEACHER", "PARENT")

                        // academic and timetable access
                        .requestMatchers(
                                "/attendance/**",
                                "/lessons/**",
                                "/timetable/**",
                                "/timetable-slots/**"
                        ).hasAnyRole("SUPER_ADMIN", "ADMIN", "STAFF", "TEACHER")

                        // everything else must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                );

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}