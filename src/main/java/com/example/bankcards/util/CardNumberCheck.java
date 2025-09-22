package com.example.bankcards.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Утилита валидации номера карты (PAN) и получения его HMAC-представления.
 * <p>
 * Назначение:
 * <ul>
 *   <li>Нормализует ввод (удаляет все нецифровые символы)</li>
 *   <li>Проверяет длину PAN согласно настройке {@code card.number.allowed-length}</li>
 *   <li>Возвращает HMAC PAN для последующего безопасного поиска в БД</li>
 * </ul>
 * Безопасность: исходный PAN не хранится и не возвращается, используется только HMAC.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CardNumberCheck {

    private final NumEncryptor encryptor;

    @Value("${card.number.allowed-length}")
    private int cardNumberLength;

    /**
     * Валидирует номер карты и возвращает его HMAC.
     * <p>
     * Шаги:
     * <ol>
     *   <li>Проверяет, что вход не {@code null}</li>
     *   <li>Удаляет все нецифровые символы</li>
     *   <li>Проверяет соответствие требуемой длине</li>
     *   <li>Возвращает HMAC PAN</li>
     * </ol>
     *
     * @param cardNumber номер карты в произвольном формате (допускаются пробелы/дефисы)
     * @return HMAC PAN (строка)
     * @throws IllegalArgumentException если номер {@code null} или длина не соответствует настройке
     */
    public String check(String cardNumber) {
        
        if (cardNumber == null) {
            throw new NullPointerException("Card number cannot be null");
        }

        String number = cardNumber.replaceAll("\\D", "");

        if (number.length() != cardNumberLength) {
            log.info("Card number length: {} allowed: {}", number.length(), cardNumberLength);
            throw new IllegalArgumentException("Invalid card number");
        }
        return encryptor.hmacPan(number);
    }

}
