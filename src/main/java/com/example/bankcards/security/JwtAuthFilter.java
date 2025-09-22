package com.example.bankcards.security;

import com.example.bankcards.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр аутентификации по JWT, выполняемый один раз за запрос.
 * <p>
 * Извлекает JWT из заголовка Authorization (схема Bearer), валидирует его,
 * загружает пользователя и, при успешной проверке, устанавливает аутентификацию
 * в SecurityContext. Пропускает запросы к публичным эндпоинтам (swagger и auth).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService uds;

    /**
     * Определяет, следует ли пропустить фильтрацию для данного запроса.
     * Пропускает документацию OpenAPI/Swagger и эндпоинты аутентификации.
     *
     * @param req текущий HTTP-запрос
     * @return true, если фильтр не должен применяться к этому запросу
     */
    @Override
    protected boolean shouldNotFilter(jakarta.servlet.http.HttpServletRequest req) {
        String uri = req.getRequestURI();
        return uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.equals("/swagger-ui.html")
                || uri.startsWith("/api/v1/auth/");
    }

    /**
     * Основная логика фильтра:
     * - Извлекает JWT из заголовка Authorization (Bearer ...).
     * - Пытается извлечь имя пользователя, загрузить {@link org.springframework.security.core.userdetails.UserDetails}
     *   и валидировать токен.
     * - При валидном токене создает {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}
     *   и помещает его в {@link org.springframework.security.core.context.SecurityContextHolder}.
     * - В любом случае передает управление дальше по цепочке фильтров.
     *
     * Исключения JWT умышленно игнорируются, чтобы не раскрывать детали ошибок и не блокировать дальнейшую обработку.
     *
     * @param req   текущий HTTP-запрос
     * @param res   текущий HTTP-ответ
     * @param chain цепочка фильтров
     * @throws java.io.IOException              при ошибках ввода/вывода
     * @throws jakarta.servlet.ServletException при ошибках обработки запроса
     */
    @Override
    protected void doFilterInternal(
            jakarta.servlet.http.HttpServletRequest req,
            jakarta.servlet.http.HttpServletResponse res,
            jakarta.servlet.FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {

        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                String email = jwtService.extractUsername(token);
                if (email != null && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null) {
                    var user = uds.loadUserByUsername(email);
                    if (jwtService.isValid(token, user)) {
                        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());
                        auth.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(req));
                        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (io.jsonwebtoken.JwtException ignored) {
            }
        }
        chain.doFilter(req, res);
    }
}
