package com.safe.system.security;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.*;
import org.springframework.security.web.csrf.*;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwords() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  UserDetailsService users(AdminRepository repository) {
    return name ->
        repository
            .findByUsername(name)
            .filter(u -> u.enabled)
            .map(u -> User.withUsername(u.username).password(u.passwordHash).roles("ADMIN").build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
  }

  @Bean
  AuthenticationManager manager(UserDetailsService users, PasswordEncoder encoder) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(encoder);
    return new ProviderManager(provider);
  }

  @Bean
  SecurityContextRepository contexts() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  CsrfTokenRepository csrf() {
    return new HttpSessionCsrfTokenRepository();
  }

  @Bean
  SecurityFilterChain chain(
      HttpSecurity http,
      SecurityContextRepository contexts,
      CsrfTokenRepository csrf,
      @Value("${safe.cors-origins}") String origins)
      throws Exception {
    var cors = new CorsConfiguration();
    cors.setAllowedOrigins(Arrays.asList(origins.split(",")));
    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cors.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN"));
    cors.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return http.cors(c -> c.configurationSource(source))
        .csrf(c -> c.csrfTokenRepository(csrf))
        .securityContext(c -> c.securityContextRepository(contexts))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/actuator/health",
                        "/api/v1/public/**",
                        "/api/v1/auth/csrf",
                        "/api/v1/auth/login")
                    .permitAll()
                    .requestMatchers(
                        "/api/v1/admin/**",
                        "/api/v1/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          res.getWriter()
                              .write(
                                  "{\"code\":\"UNAUTHENTICATED\",\"message\":\"Faça login para continuar\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setContentType("application/json");
                          res.getWriter()
                              .write(
                                  "{\"code\":\"FORBIDDEN\",\"message\":\"Acesso negado ou CSRF inválido\"}");
                        }))
        .headers(
            h ->
                h.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; object-src 'none'; frame-ancestors 'none'; style-src 'self' 'unsafe-inline'")))
        .formLogin(c -> c.disable())
        .httpBasic(c -> c.disable())
        .logout(c -> c.disable())
        .build();
  }
}
