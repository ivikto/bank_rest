package com.example.bankcards.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Утилита для криптографической обработки PAN (номера банковской карты).
 * <p>
 * Предоставляет:
 * - Симметричное шифрование/дешифрование с использованием AES/GCM/NoPadding.
 * - Вычисление HMAC-SHA256 для детерминированного хеширования PAN.
 * <p>
 * Безопасность:
 * - Для AES-GCM генерируется случайный IV длиной 12 байт на каждое шифрование.
 * - Результат шифрования кодируется Base64 и содержит IV префиксом перед ciphertext+tag.
 * - HMAC-SHA256 возвращается в Base64.
 * <p>
 * Валидация:
 * - Методы принимают PAN не короче 16 символов; при нарушении выбрасывается IllegalArgumentException.
 */
public class NumEncryptor {

    private static final String AES_ALG = "AES/GCM/NoPadding";
    private static final int GCM_IV_LEN = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecretKey aesKey;
    private final SecretKey hmacKey;
    private final SecureRandom rnd = new SecureRandom();
    
    private final String WARN_MSG = "Validation error: bad entry data {}";

    /**
     * Шифрует PAN с помощью AES-GCM.
     * Формат результата: Base64(IV[12] || ciphertext||tag).
     *
     * @param pan PAN (минимум 16 символов, ASCII)
     * @return строка Base64 с префиксом IV и шифртекстом
     * @throws IllegalArgumentException если pan null или короче 16 символов
     * @throws IllegalStateException при ошибке криптографических операций
     */
    public String encryptPan(String pan) {
        if (pan == null || pan.length() < 16) {
            log.warn(WARN_MSG, pan);
            throw new IllegalArgumentException("Bad pan");
        }
        
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            rnd.nextBytes(iv);
            Cipher c = Cipher.getInstance(AES_ALG);
            c.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = c.doFinal(pan.getBytes(StandardCharsets.US_ASCII));
            byte[] out = ByteBuffer.allocate(iv.length + ct.length).put(iv).put(ct).array();
            return Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PAN encryption failed", e);
        }
    }

    /**
     * Вычисляет HMAC-SHA256 от PAN.
     *
     * @param pan PAN (минимум 16 символов, ASCII)
     * @return Base64-представление HMAC-SHA256
     * @throws IllegalArgumentException если pan null или короче 16 символов
     * @throws IllegalStateException при ошибке криптографических операций
     */
    public String hmacPan(String pan) {
        if (pan == null || pan.length() < 16) {
            log.warn(WARN_MSG, pan);
            throw new IllegalArgumentException("Bad pan");
        }
        
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            return Base64.getEncoder().encodeToString(mac.doFinal(pan.getBytes(StandardCharsets.US_ASCII)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PAN HMAC failed", e);
        }

    }

    /**
     * Дешифрует PAN, зашифрованный методом encryptPan.
     *
     * Ожидаемый ввод: Base64(IV[12] || ciphertext||tag) для AES/GCM/NoPadding.
     *
     * @param packedBase64 строка Base64 с IV и шифртекстом
     * @return исходный PAN в кодировке UTF-8
     * @throws IllegalArgumentException если входная строка пуста или null
     * @throws Exception при ошибке дешифрования (включая аутентификацию GCM)
     */
    public String decryptPan(String packedBase64) throws Exception {
        if (packedBase64 == null || packedBase64.length() <= 0) {
            log.warn(WARN_MSG, packedBase64);
            throw new IllegalArgumentException("Bad packedBase64");
        }
        
        
        byte[] data = Base64.getDecoder().decode(packedBase64);
        byte[] iv = Arrays.copyOfRange(data, 0, 12);
        byte[] ct = Arrays.copyOfRange(data, 12, data.length);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] panBytes = c.doFinal(ct);
        return new String(panBytes, StandardCharsets.UTF_8);
    }
}
