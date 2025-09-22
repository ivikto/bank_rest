package com.example.bankcards.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки для JWT-аутентификации.
 * <p>
 * Значения берутся из {@code application.yml} / {@code application.properties}
 * с префиксом {@code security.jwt}.
 *
 * <pre>
 * Пример конфигурации:
 * security:
 *   jwt:
 *     secret-base64: "YmFzZTY0LWVuY29kZWQtc2VjcmV0LWtleQ=="
 *     expiration-minutes: 60
 * </pre>
 *
 * @param secretBase64       секретный ключ в Base64 (используется для подписи JWT)
 * @param expirationMinutes  срок жизни токена в минутах
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProps(String secretBase64, long expirationMinutes) { }
