package com.example.bankcards.util;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Компонент для генерации уникальных номеров карт (PAN).
 * <p>
 * Номер карты формируется из:
 * <ul>
 *   <li>Банковского идентификатора {@link #BANK_ID}</li>
 *   <li>Значения последовательности из БД (seq card_account_seq)</li>
 *   <li>Контрольной цифры по алгоритму Луна</li>
 * </ul>
 * <p>
 * Пример результата: 2200700000001234
 */
@Component
@RequiredArgsConstructor
public class NumGenerator {

    /**
     * Банковский идентификатор (первые 6 цифр PAN).
     */
    private static final String BANK_ID = "220070";

    /**
     * Шаблон для выполнения SQL-запросов.
     */
    private final JdbcTemplate jdbc;

    /**
     * Генерирует уникальный номер карты.
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>Берётся следующее значение последовательности {@code card_account_seq} в базе</li>
     *   <li>Значение форматируется в строку фиксированной длины (9 цифр с лидирующими нулями)</li>
     *   <li>К строке добавляется банк-ID</li>
     *   <li>Вычисляется контрольная цифра по алгоритму Луна</li>
     *   <li>Все части склеиваются в итоговый номер</li>
     * </ol>
     *
     * @return сгенерированный PAN (номер карты) с контрольной цифрой
     */
    public String generateNum() {
        Long sequenceValue = jdbc.queryForObject("select nextval('card_account_seq')", Long.class);
        String formatedSequenceVal = String.format("%09d", sequenceValue);
        return (BANK_ID + formatedSequenceVal) + luhnCheckDigit(BANK_ID + formatedSequenceVal);
    }

    /**
     * Вычисляет контрольную цифру по алгоритму Луна.
     * <p>
     * Метод используется для валидации корректности PAN.
     *
     * @param number строка с числовым значением без контрольной цифры
     * @return символ контрольной цифры
     */
    private char luhnCheckDigit(String number) {
        int sum = 0; boolean dbl = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (dbl) { n *= 2; if (n > 9) n -= 9; }
            sum += n; dbl = !dbl;
        }
        return (char) ('0' + ((10 - (sum % 10)) % 10));
    }
}
