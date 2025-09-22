package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация Spring Security для REST-приложения.
 *
 * <p>Особенности:
 * <ul>
 *   <li>Stateless-сессии и отключённый CSRF;</li>
 *   <li>JWT-аутентификация через фильтр в цепочке;</li>
 *   <li>Явные разрешения для Swagger/OpenAPI и auth-эндпоинтов;</li>
 *   <li>Кастомные обработчики 401/403 с JSON-ответом;</li>
 *   <li>Методовая безопасность через @PreAuthorize/@PostAuthorize.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity // включает поддержку аннотаций @PreAuthorize / @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwt;

    /**
     * Конфигурирует цепочку фильтров безопасности.
     *
     * @param http объект {@link HttpSecurity}, предоставляемый Spring Security
     * @return настроенный SecurityFilterChain
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/auth/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                )
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Бин кодировщика паролей.
     *
     * <p>Использует BCrypt с параметрами по умолчанию.
     *
     * @return PasswordEncoder для хеширования паролей
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Бин AuthenticationManager.
     *
     * <p>Получается из AuthenticationConfiguration, учитывает все зарегистрированные
     * AuthenticationProvider.
     *
     * @param cfg конфигурация аутентификации
     * @return менеджер аутентификации
     * @throws Exception при ошибке инициализации
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * Обработчик 401 Unauthorized для REST.
     *
     * <p>Возвращает JSON-ответ с базовой диагностикой без утечки деталей.
     *
     * @return AuthenticationEntryPoint
     */
    @Bean
    AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String path = request.getRequestURI();
            String body = """
                {"timestamp":"%s","status":401,"error":"Unauthorized","message":"%s","path":"%s"}
                """.formatted(java.time.OffsetDateTime.now(), safeMsg(authException), path);
            response.getWriter().write(body);
        };
    }

    /**
     * Обработчик 403 Forbidden для REST.
     *
     * <p>Возвращает JSON-ответ с коротким сообщением об отказе в доступе.
     *
     * @return AccessDeniedHandler
     */
    @Bean
    AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            String path = request.getRequestURI();
            String body = """
                {"timestamp":"%s","status":403,"error":"Forbidden","message":"%s","path":"%s"}
                """.formatted(java.time.OffsetDateTime.now(), safeMsg(accessDeniedException), path);
            response.getWriter().write(body);
        };
    }

    /**
     * Безопасно нормализует сообщение исключения для клиента.
     *
     * @param ex исходное исключение
     * @return безопасное сообщение (дефолт: "Access denied")
     */
    private static String safeMsg(Exception ex) {
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? "Access denied" : m;
    }
}
