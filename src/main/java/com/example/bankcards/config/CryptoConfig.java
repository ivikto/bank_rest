package com.example.bankcards.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Конфигурация криптографических ключей приложения.
 * <p>
 * Читает ключи из application.properties / application.yml (в Base64-кодировке)
 * и предоставляет их как Spring-бины для использования в сервисах.
 * <ul>
 *   <li>{@code crypto.number.aes-key-base64} → AES-ключ</li>
 *   <li>{@code crypto.number.hmac-key-base64} → HMAC-SHA256-ключ</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class CryptoConfig {

    @Value("${crypto.number.aes-key-base64}")
    private String aesKeyB64;

    @Value("${crypto.number.hmac-key-base64}")
    private String hmacKeyB64;

    /**
     * Декодирует и возвращает симметричный AES-ключ.
     * <p>
     * Ключ используется, например, для шифрования/дешифрования номеров карт.
     *
     * @return {@link SecretKey} для алгоритма AES
     * @throws IllegalArgumentException если строка не является корректным Base64
     */
    @Bean
    public SecretKey aesKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(aesKeyB64), "AES");
    }

    /**
     * Декодирует и возвращает HMAC-ключ для вычисления контрольных сумм.
     * <p>
     * Применяется в алгоритме {@code HmacSHA256}, например,
     * для защиты номеров карт или проверки целостности данных.
     *
     * @return {@link SecretKey} для алгоритма HmacSHA256
     * @throws IllegalArgumentException если строка не является корректным Base64
     */
    @Bean
    public SecretKey hmacKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(hmacKeyB64), "HmacSHA256");
    }
}
