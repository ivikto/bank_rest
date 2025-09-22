package com.example.bankcards.service;

import com.example.bankcards.config.props.JwtProps;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;


/**
 * Сервис для выпуска и валидации JWT.
 * <p>
 * Использует симметричный HMAC-ключ (передаётся в Base64) и срок жизни,
 * задаваемый в минутах. Токены подписываются через {@code io.jsonwebtoken} (jjwt),
 * в payload добавляется subject (username) и claim {@code roles}.
 * <p>
 * Потокобезопасен после инициализации: поля неизменяемые, {@link SecretKey} создаётся один раз.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    /**
     * @param props конфигурация JWT: Base64-ключ и срок жизни в минутах
     * @throws IllegalArgumentException если секрет не валиден (не Base64 или слишком короткий для HMAC)
     */
    public JwtService(JwtProps props) {
        if (props == null) {
            log.warn("Validation error: JwtProps is null");
            throw new NullPointerException("JwtProps is null");
        }
        final byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(props.secretBase64());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 secret", e);
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = props.expirationMinutes() * 60_000L;
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("expirationMinutes must be > 0");
        }
    }

    /**
     * Генерирует подписанный JWT для пользователя.
     * <ul>
     *   <li>subject = {@code user.getUsername()}</li>
     *   <li>claim {@code roles} = список {@code GrantedAuthority#getAuthority()}</li>
     *   <li>устанавливаются {@code iat} и {@code exp}</li>
     * </ul>
     *
     * @param user пользователь, для которого выпускается токен
     * @return компактная строка JWT
     * @throws IllegalArgumentException если {@code user} равен null или у него пустой username
     */
    public String generateToken(UserDetails user) {
        if (user == null) {
            log.warn("Validation error: User is null");
            throw new NullPointerException("User is null");
        }
        final String username = user.getUsername();
        if (username == null || username.isBlank()) {
            log.warn("Validation error: username is blank");
            throw new IllegalArgumentException("Username is blank");
        }

        final List<String> roles = user.getAuthorities() == null
                ? List.of()
                : user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    /**
     * Извлекает {@code subject} (username) из подписанного JWT.
     *
     * @param token компактная строка JWT
     * @return имя пользователя из {@code sub}
     * @throws IllegalArgumentException если токен пустой/нулевой
     * @throws JwtException             если подпись невалидна, формат плохой или токен просрочен
     */
    public String extractUsername(String token) {
        if (token == null || token.isBlank()) {
            throw new NullPointerException("Token is null or blank");
        }
        var jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return jws.getPayload().getSubject();
    }

    /**
     * Проверяет, что токен принадлежит пользователю и ещё действителен.
     * <p>
     * Метод <b>не</b> бросает исключения: любые ошибки парсинга/подписи/срока считаются недействительным токеном.
     *
     * @param token компактная строка JWT
     * @param user  ожидаемый владелец токена
     * @return {@code true}, если подпись валидна, не истёк {@code exp}, и {@code sub} совпадает с {@code user.getUsername()}
     */
    public boolean isValid(@Nullable String token, @Nullable UserDetails user) {
        if (token == null || token.isBlank() || user == null) return false;
        final String expected = user.getUsername();
        if (expected == null || expected.isBlank()) return false;

        try {
            var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return expected.equals(claims.getSubject())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            // подпись/формат/иные ошибки
            log.debug("JWT invalid: {}", e.getMessage());
            return false;
        }
    }
}
