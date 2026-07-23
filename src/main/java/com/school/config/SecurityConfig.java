package com.school.config;
import com.school.security.service.JwtAuthFilter;
import com.school.security.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http   .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers( "/login","/complete-registration","/stk/callback","/stkPush").permitAll()

                        .requestMatchers(HttpMethod.POST, "/assigns","/funds","/classes","/students","/subjects","/term","/funds/bulk").hasAnyRole("ADMIN","SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/assign/{assignmentId}","/parent/id/{parentId}","/parent/update/{parentId}").hasAnyRole("ADMIN","SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/parent/id/{parentId}").hasAnyRole("ADMIN","SUPER_ADMIN")
                        //.requestMatchers(HttpMethod.GET,"/assignments","/assign/id/{assignmentId}","/{studentId}/funds","/parents","/parent/id/{parentId}","/parent/name/{name}","/classes","/students","/subjects","/teachers","/teacher/id/{teacherId}","/teacher/{name}","/terms").hasAnyRole("SUPER_ADMIN","ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/finance/summary").hasAnyRole("SUPER_ADMIN","ADMIN")

                        .requestMatchers(HttpMethod.GET,"/assignments","/assign/id/{assignmentId}","/{studentId}/funds",
                                "/parents","/parent/id/{parentId}","/parent/name/{name}").hasAnyRole("SUPER_ADMIN","ADMIN")
                        .requestMatchers(HttpMethod.GET,"/classes","/students","/subjects","/teachers",
                                "/teacher/id/{teacherId}","/teacher/{name}").hasAnyRole("SUPER_ADMIN","ADMIN","TEACHER")
                        .requestMatchers(HttpMethod.GET,"/terms").hasAnyRole("SUPER_ADMIN","ADMIN")

                        .requestMatchers("/admin/invite-user").hasRole("SUPER_ADMIN")
                        .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")


                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}