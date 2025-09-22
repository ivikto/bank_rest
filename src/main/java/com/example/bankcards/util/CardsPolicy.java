package com.example.bankcards.util;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.exception.IdenticalCardsException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.TransferAccessException;
import com.example.bankcards.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Утилита проверок для операций перевода средств.
 * <p>
 * Назначение:
 * <ul>
 *   <li>Валидация, что источник и назначение перевода — разные карты</li>
 *   <li>Проверка активного статуса карт</li>
 *   <li>Проверка, что обе карты принадлежат текущему пользователю</li>
 * </ul>
 * При нарушении правил записывает предупреждение в журнал и выбрасывает соответствующее исключение.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransferPolicy {

    private  final SecurityUtils securityUtils;

    /**
     * Проверяет, что источник и назначение перевода — разные карты.
     *
     * Правило: идентификаторы карт в {@link TransferDto#sourceCardId()} и
     * {@link TransferDto#destinationCardId()} не должны совпадать.
     *
     * При нарушении логирует предупреждение и выбрасывает {@link IllegalArgumentException}.
     *
     * @param transferDto данные перевода
     * @throws IllegalArgumentException если указаны одинаковые карты
     */
    public void assertNotSameCards(TransferDto transferDto) {

        if (transferDto.sourceCardId().equals(transferDto.destinationCardId())) {
            log.warn("Validation failed: same source and destination card: {}", transferDto.sourceCardId());
            throw new IdenticalCardsException("Source card and destination card must differ");
        }
    }

    /**
     * Проверяет, что карта активна.
     *
     * Правило: {@link BaseCard#getCardStatus()} должен быть равен {@link CardStatus#ACTIVE}.
     *
     * При нарушении логирует предупреждение и выбрасывает {@link IllegalStateException}.
     *
     * @param card карта для проверки
     * @throws IllegalStateException если статус карты не ACTIVE
     */
    public void assertActive(BaseCard card) {
        if (card.getCardStatus() != CardStatus.ACTIVE) {
            log.warn("Validation failed: some card is not active. sourceCard: {} ", card.getCardStatus());
            throw new CardStatusException("Cards must be ACTIVE");
        }
    }

    /**
     * Проверяет, что обе карты принадлежат текущему аутентифицированному пользователю.
     *
     * Правило: идентификатор владельца у обеих карт должен совпадать с текущим пользователем,
     * полученным из {@link SecurityUtils#currentUserId()}.
     *
     * При нарушении логирует предупреждение и выбрасывает {@link AccessDeniedException}.
     *
     * @param sourceCard      карта-источник
     * @param destinationCard карта-назначение
     * @throws AccessDeniedException если хотя бы одна карта не принадлежит текущему пользователю
     */
    public void assertOwnedByUser(BaseCard sourceCard, BaseCard destinationCard ) {
        Long currentUserId = securityUtils.currentUserId();

        if (sourceCard.getUser().getId() != currentUserId) {
            log.warn("Access denied: attempt to transfer to a third-party account. CardId {}", sourceCard.getId());
            throw new TransferAccessException("Access denied: Transfers only on your own accounts");
        }

        if (destinationCard.getUser().getId() != currentUserId) {
            log.warn("Access denied: attempt to transfer to a third-party account. CardId {}", destinationCard.getId());
            throw new TransferAccessException("Access denied: Transfers only on your own accounts");
        }

    }

    /**
     * Проверяет, что обе карты принадлежат текущему аутентифицированному пользователю.
     *
     * Правило: идентификатор владельца у обеих карт должен совпадать с текущим пользователем,
     * полученным из {@link SecurityUtils#currentUserId()}.
     *
     * При нарушении логирует предупреждение и выбрасывает {@link AccessDeniedException}.
     *
     * @param card     карта
     * @throws AccessDeniedException если хотя бы одна карта не принадлежит текущему пользователю
     */
    public void assertOwnedByUser(BaseCard card) {
        Long currentUserId = securityUtils.currentUserId();

        if (card.getUser().getId() != currentUserId) {
            log.warn("Access denied: attempt to transfer to a third-party account. CardId {}", card.getId());
            throw new TransferAccessException("Access denied: Transfers only on your own accounts");
        }
    }



    /**
     * Проверяет достаточность средств на карте-источнике для списания.
     *
     * Правила:
     * - Сумма перевода должна быть положительной (amount > 0).
     * - Баланс карты-источника должен быть инициализирован.
     * - Баланс должен быть не меньше суммы списания.
     *
     * Исключения:
     * - IllegalArgumentException — если сумма не задана или неположительная.
     * - IllegalStateException — если баланс карты не инициализирован.
     * - InsufficientFundsException — если баланс меньше суммы списания.
     *
     * @param amount      сумма списания
     * @param sourceCard  карта-источник средств
     */
    public void assertInsufficientFunds(BigDecimal amount, BaseCard sourceCard) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        BigDecimal balance = sourceCard.getBalance();
        if (balance == null) {
            throw new IllegalStateException("Card balance is not initialized");
        }

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds: balance=" + balance + ", amount=" + amount
            );
        }
    }
}
