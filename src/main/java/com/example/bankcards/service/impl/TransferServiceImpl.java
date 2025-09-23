package com.example.bankcards.service.impl;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferResultDto;
import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.mapper.BankMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.SecurityUtils;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.CardNumberCheck;
import com.example.bankcards.util.CardsPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Сервис переводов и получения баланса.
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>Перевод средств между картами текущего пользователя с блокировкой записей (select for update)</li>
 *   <li>Получение баланса по ID карты и по номеру карты (через HMAC)</li>
 *   <li>Валидацию входных данных, статусов карт и проверку прав доступа</li>
 *   <li>Логирование ключевых событий и ошибок</li>
 * </ul>
 * Зависимости:
 * <ul>
 *   <li>{@link CardRepository} — доступ к данным карт, методы с блокировкой</li>
 *   <li>{@link BankMapper} — маппинг сущностей в DTO</li>
 *   <li>{@link CardNumberCheck} — проверка и HMAC номера карты</li>
 *   <li>{@link SecurityUtils} — получение текущего пользователя и его прав</li>
 * </ul>
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final BankMapper mapper;
    private final CardNumberCheck cardNumberCheck;
    private final SecurityUtils securityUtils;
    private final CardService cardService;
    private final CardsPolicy cardsPolicy;


    /**
     * Перевод средств между двумя картами текущего пользователя.
     * <p>
     * Правила и проверки:
     * <ul>
     *   <li>Сумма > 0</li>
     *   <li>Источник и назначение — разные карты</li>
     *   <li>Обновление карт выполняется под блокировкой (findByIdForUpdate)</li>
     *   <li>Обе карты принадлежат одному пользователю и это — текущий пользователь</li>
     *   <li>Обе карты в статусе ACTIVE</li>
     *   <li>На исходной карте достаточно средств</li>
     * </ul>
     *
     * Транзакционная граница: метод помечен {@code @Transactional}, изменения фиксируются/откатываются атомарно.
     * Isolation.READ_COMMITTED - в рамках транзакции видны только сохраненные изменения
     *
     * @param transferDto данные перевода: ID исходной и целевой карт, сумма
     * @return результат перевода с актуальными данными обеих карт
     * @throws IllegalArgumentException при некорректных входных данных
     * @throws CardNotFoundException если одна из карт не найдена
     * @throws InsufficientFundsException если недостаточно средств на исходной карте
     * @throws org.springframework.security.access.AccessDeniedException если нарушены права доступа
     * @throws IllegalStateException если карты не в статусе ACTIVE
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 5)
    public TransferResultDto balanceTransfer(TransferDto transferDto) {

        cardsPolicy.assertNotSameCards(transferDto);
        BigDecimal amount = transferDto.amount().setScale(2, RoundingMode.DOWN);

        Long currentUserId = securityUtils.currentUserId();
        log.info("Transfer started: userId={}, sourceCardId={}, destinationCardId={}, amount={}",
                currentUserId, transferDto.sourceCardId(), transferDto.destinationCardId(), amount);

        Long minId = Math.min(transferDto.sourceCardId(), transferDto.destinationCardId());
        Long maxId = Math.max(transferDto.sourceCardId(), transferDto.destinationCardId());

        BaseCard firstCard = cardService.findCardByIdForUpdate(minId);
        BaseCard secondCard = cardService.findCardByIdForUpdate(maxId);

        BaseCard sourceCard = transferDto.sourceCardId().equals(minId) ? firstCard : secondCard;
        BaseCard destinationCard = transferDto.destinationCardId().equals(minId) ? firstCard : secondCard;

        cardsPolicy.assertOwnedByUser(sourceCard, destinationCard);
        cardsPolicy.assertActive(sourceCard);
        cardsPolicy.assertActive(destinationCard);
        cardsPolicy.assertInsufficientFunds(amount, sourceCard);

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        destinationCard.setBalance(destinationCard.getBalance().add(amount));

        return new TransferResultDto(
                mapper.cardToCardDto(sourceCard),
                mapper.cardToCardDto(destinationCard));
    }

    /**
     * Возвращает баланс карты по её идентификатору.
     * <p>
     * Доступ только владельцу карты. Чтение в рамках read-only транзакции.
     *
     * @param cardId идентификатор карты
     * @return баланс карты
     * @throws IllegalArgumentException если {@code cardId} равен null
     * @throws CardNotFoundException если карта не найдена
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не владелец
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long cardId) {
        if (cardId == null) {
            log.warn("Validation failed: cardId is null");
            throw new IllegalArgumentException("Card id cannot be null");
        }

        BaseCard card = cardService.getBaseCard(cardId);

        cardsPolicy.assertOwnedByUser(card);
        return card.getBalance();
    }
    /**
     * Возвращает баланс карты по её номеру.
     * <p>
     * Номер валидируется и преобразуется в HMAC, поиск выполняется по HMAC.
     * Доступ только владельцу карты. Чтение в рамках read-only транзакции.
     *
     * @param cardNumber номер карты (PAN), допустимая длина определяется конфигурацией
     * @return баланс карты
     * @throws IllegalArgumentException при некорректном номере карты
     * @throws CardNotFoundException если карта не найдена
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не владелец
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String cardNumber) {
        Long currentUserId = securityUtils.currentUserId();

        String numHmac = cardNumberCheck.check(cardNumber);

        BaseCard card = cardService.findByCardNumber(numHmac);

        if (card.getUser().getId() != currentUserId) {
            log.warn("Access denied: operation only for card {} owner", cardNumber);
            throw new AccessDeniedException("Access denied");
        }
        return card.getBalance();
    }

}
