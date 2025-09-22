package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferResultDto;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * Операции перевода и получения баланса по картам.
 *
 * <p>Безопасность/доступы рекомендуется проверять выше по слою
 * (контроллер/метод-секьюрити). Валидацию DTO — аннотациями Bean Validation
 * на реализации + {@code @Validated} на классе.
 */
@Validated
public interface TransferService {

    /**
     * Перевод средств между картами.
     *
     * <p>Ожидается, что реализация:
     * <ul>
     *   <li>валидирует вход (ID &gt; 0, сумма &gt; 0);</li>
     *   <li>проверяет существование обеих карт;</li>
     *   <li>проверяет достаточность средств на источнике;</li>
     *   <li>выполняет перевод в одной транзакции с корректной блокировкой строк
     *       (во избежание гонок/двойного списания);</li>
     *   <li>возвращает итоговые балансы и метаданные операции.</li>
     * </ul>
     *
     * @param transferDto параметры перевода (не {@code null})
     * @return результат перевода
     * @throws IllegalArgumentException      при некорректных аргументах (null, сумма ≤ 0 и т.д.)
     * @throws CardNotFoundException         если одна из карт не найдена
     * @throws InsufficientFundsException    если средств на исходной карте недостаточно
     */
    TransferResultDto balanceTransfer(@Valid TransferDto transferDto);

    /**
     * Текущий баланс по ID карты.
     *
     * @param cardId ID карты (&gt; 0)
     * @return баланс
     * @throws IllegalArgumentException если {@code cardId} некорректен
     * @throws CardNotFoundException    если карта не найдена или недоступна текущему пользователю
     */
    BigDecimal getBalance(@NotNull @Positive Long cardId);

    /**
     * Текущий баланс по номеру карты (PAN).
     *
     * <p>Ожидается, что реализация валидирует PAN и ищет по HMAC/токенизированному значению.
     *
     * @param cardNumber полный номер карты (PAN), 16 цифр
     * @return баланс
     * @throws IllegalArgumentException если номер null/пустой/невалидный
     * @throws CardNotFoundException    если карта не найдена или недоступна текущему пользователю
     */
    BigDecimal getBalance(@NotBlank String cardNumber);
}
