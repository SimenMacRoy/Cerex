package com.cerex.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration.
 *
 * <p>Implements:
 * <ul>
 *   <li>Stateless JWT authentication</li>
 *   <li>BCrypt password encoding (cost=12)</li>
 *   <li>Method-level authorization (@PreAuthorize)</li>
 *   <li>CORS configuration</li>
 *   <li>Public endpoints for auth, docs, and health</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CerexUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/ws/**", "/ws-native/**").permitAll()

                // Public read-only recipe endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/recipes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/explore/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/recipes").permitAll()

                // Public read-only restaurant & grocery endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/restaurants/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/groceries/**").permitAll()

                // Public social feed & posts
                .requestMatchers(HttpMethod.GET, "/api/v1/social/feed/trending").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/social/posts/{postId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/social/users/{userId}/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/social/hashtags/**").permitAll()

                // Public eco community stats
                .requestMatchers(HttpMethod.GET, "/api/v1/eco/community-stats").permitAll()

                // Admin endpoints
                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/restaurants/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/v1/groceries/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
