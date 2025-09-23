package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequestDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.factory.CardFactory;
import com.example.bankcards.mapper.BankMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.spec.CardSpecs;
import com.example.bankcards.security.SecurityUtils;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberCheck;
import com.example.bankcards.util.CardsPolicy;
import com.example.bankcards.util.CardsSearchFilterPolicy;
import com.example.bankcards.util.PageableBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * Сервис работы с картами.
 * <p>
 * Функциональность:
 * <ul>
 *   <li>Создание карты пользователю с защитой от коллизий (повторные попытки при уникальных ограничениях)</li>
 *   <li>Поиск карт с пагинацией/сортировкой и фильтрами; ограничение выборки правами пользователя</li>
 *   <li>Получение карты по ID и по номеру (поиск через HMAC PAN) с проверкой владельца</li>
 *   <li>Частичное обновление статуса/владельца карты под блокировкой строки</li>
 *   <li>Удаление карты</li>
 * </ul>
 * Все операции используют транзакции Spring и ведут журнал ошибок/валидации.
 * Зависимости:
 * <ul>
 *   <li>{@link CardRepository} — доступ к данным карт</li>
 *   <li>{@link UserRepository} — доступ к данным пользователей</li>
 *   <li>{@link CardFactory} — фабрика создания новых карт</li>
 *   <li>{@link BankMapper} — маппинг сущностей в DTO</li>
 *   <li>{@link SecurityUtils} — текущий пользователь и его роли</li>
 *   <li>{@link CardNumberCheck} — валидация PAN и получение HMAC</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardFactory cardFactory;
    private final BankMapper mapper;
    private final SecurityUtils securityUtils;
    private final CardNumberCheck cardNumberCheck;
    private final CardsSearchFilterPolicy searchPolicy;
    private final CardsPolicy cardsPolicy;

    /**
     * Создает карту для пользователя.
     * <p>
     * Выполняет до 3 попыток сохранения на случай коллизий уникальных полей.
     *
     * @param dto содержит ID пользователя-владельца
     * @return созданная карта в виде DTO
     * @throws IllegalArgumentException                                если {@code userId} равен 0
     * @throws UserNotFoundException                                   если пользователь не найден
     * @throws org.springframework.dao.DataIntegrityViolationException если не удалось сохранить после повторных попыток
     */
    @Override
    @Transactional
    public CardDto createCard(CardCreateDto dto) {

        BaseUser user = userRepository.findById(dto.userId())
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", dto.userId());
                    return new UserNotFoundException("User not found + " + dto.userId());
                });

        final int MAX_RETRY = 3;

        for (int i = 0; i < MAX_RETRY; i++) {
            BaseCard card = cardFactory.createCard(user);
            try {
                card = cardRepository.saveAndFlush(card);
                return mapper.cardToCardDto(card);
            } catch (DataIntegrityViolationException e) {
                if (i == MAX_RETRY - 1) throw e;
            }
        }
        log.error("Card repository is unreachable");
        throw new IllegalStateException("unreachable");
    }

    /**
    /**
     * Возвращает страницу карт по фильтрам и пагинации.
     * <p>
     * Некорректные параметры валидируются. Если текущий пользователь не админ,
     * выборка автоматически ограничивается его собственными картами.
     *
     * @param dto объект запроса с пагинацией, сортировкой и фильтрами:
     *            page (>= 0), size [1..100], sort, userId (для админа),
     *            last4 (ровно 4 цифры), status, expirationFrom/to,
     *            balanceMin/max, createdFrom/to
     * @return страница DTO карт
     * @throws IllegalArgumentException при нарушении правил валидации параметров или если dto == null
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getCards(CardSearchRequestDto dto) {

        if (dto == null) {
            log.warn("Validation failed: dto cannot be null");
            throw new IllegalArgumentException("dto cannot be null");
        }

        final int page = (dto.page() == null || dto.page() < 0) ? 0 : dto.page();
        final int sizeRaw = (dto.size() == null || dto.size() < 1) ? 20 : dto.size();
        final int size = Math.min(sizeRaw, 100);
        final String sort = (dto.sort() == null || dto.sort().isBlank()) ? "createdAt,desc" : dto.sort();

        searchPolicy.checkFilter(dto);

        Pageable pageable = PageableBuilder.build(page, size, sort);

        Specification<BaseCard> spec = Specification.allOf(
                CardSpecs.userIdEq(dto.userId()),
                CardSpecs.last4Eq(dto.last4()),
                CardSpecs.statusEq(dto.status()),
                CardSpecs.expirationFrom(dto.expirationFrom()),
                CardSpecs.expirationTo(dto.expirationTo()),
                CardSpecs.balanceGte(dto.balanceMin()),
                CardSpecs.balanceLte(dto.balanceMax()),
                CardSpecs.createdFrom(dto.createdFrom()),
                CardSpecs.createdTo(dto.createdTo())
        );

        if (!securityUtils.isAdmin()) {
            Long currentUserId = securityUtils.currentUserId();
            spec = spec.and(CardSpecs.userIdEq(currentUserId));

        }

        return cardRepository.findAll(spec, pageable)
                .map(mapper::cardToCardDto);
    }

    /**
     * Возвращает карту по ID.
     * <p>
     * Доступ только владельцу карты.
     *
     * @param cardId ID карты
     * @return DTO карты
     * @throws IllegalArgumentException                                  если {@code cardId} равен null
     * @throws CardNotFoundException                                     если карта не найдена
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не владелец
     */
    @Override
    @Transactional(readOnly = true)
    public CardDto getCard(Long cardId) {
        BaseCard card = cardRepository.findById(cardId).orElseThrow(() ->
        {
            log.warn("Card with id {} not found", cardId);
            return new CardNotFoundException("Card not found + " + cardId);
        });

        cardsPolicy.assertOwnedByUser(card);

        return mapper.cardToCardDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseCard getBaseCard(Long cardId) {
        if (cardId == null) {
            log.warn("Validation failed: Card id cannot be null");
            throw new IllegalArgumentException(
                    "Card id cannot be null");
        }

        BaseCard card = cardRepository.findById(cardId).orElseThrow(() ->
        {
            log.warn("Card with id {} not found", cardId);
            return new CardNotFoundException("Card not found + " + cardId);
        });

        cardsPolicy.assertOwnedByUser(card);

        return card;
    }

    /**
     * Возвращает карту по номеру.
     * <p>
     * PAN валидируется и преобразуется в HMAC, поиск выполняется по HMAC.
     * Доступ только владельцу карты.
     *
     * @param cardNumber номер карты (PAN)
     * @return DTO карты
     * @throws IllegalArgumentException                                  при {@code cardNumber == null} или некорректном формате
     * @throws CardNotFoundException                                     если карта не найдена
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не владелец
     */
    @Override
    @Transactional(readOnly = true)
    public CardDto getCard(String cardNumber) {
        if (cardNumber == null) {
            log.warn("Validation failed: Card number cannot be null");
            throw new IllegalArgumentException(
                    "Card id cannot be null");
        }

        String numHmac = cardNumberCheck.check(cardNumber);

        BaseCard card = (BaseCard) cardRepository.findByNumHmac(numHmac)
                .orElseThrow(() -> {
                    log.warn("Card with number {} not found", cardNumber);
                    return new CardNotFoundException("Card not found + " + numHmac);
                });

        cardsPolicy.assertOwnedByUser(card);

        return mapper.cardToCardDto(card);
    }


    /**
     * Частично обновляет карту.
     * <p>
     * Обновляются: статус карты и/или владелец. При смене владельца проверяется существование пользователя.
     * Объект блокируется на запись для согласованности.
     *
     * @param cardUpdateDto данные для обновления
     * @return обновленная карта в виде DTO
     * @throws IllegalArgumentException при некорректных входных данных
     * @throws CardNotFoundException    если карта не найдена
     * @throws UserNotFoundException    если новый владелец не найден
     */
    @Transactional
    @Override
    public CardDto updateCard(CardUpdateDto cardUpdateDto) {
        if (cardUpdateDto == null) {
            log.warn("Validation failed: cardUpdateDto cannot be null");
            throw new IllegalArgumentException("cardUpdateDto cannot be null");
        }

        Long cardId = cardUpdateDto.cardId();

        if (cardId == null) {
            log.warn("Validation failed: cardId  cannot be null");
            throw new IllegalArgumentException("cardId cannot be null");
        }

        CardStatus newStatus = cardUpdateDto.status();

        BaseCard card = cardRepository.findByIdForUpdate(cardId)
                .orElseThrow(() -> {
                    log.warn("Card with id {} not found", cardId);
                    return new CardNotFoundException("Card not found + " + cardId);
                });

        if (newStatus != null && newStatus != card.getCardStatus()) {
            card.setCardStatus(newStatus);
        }

        Long userId = cardUpdateDto.userId();

        if (userId != null && userId != 0 && !userId.equals(card.getUser().getId())) {
            BaseUser user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User with id {} not found", userId);
                        return new UserNotFoundException("User not found + " + userId);
                    });

            card.setUser(user);
        }

        card.setModifiedAt(LocalDateTime.now());

        return mapper.cardToCardDto(card);
    }

    /**
     * Удаляет карту по ID.
     *
     * @param cardId ID карты
     * @throws IllegalArgumentException если {@code cardId} равен null
     * @throws CardNotFoundException    если карта не найдена
     */
    @Transactional
    @Override
    public void deleteCard(Long cardId) {
        if (cardId == null) {
            log.warn("Validation failed: Card id cannot be null");
            throw new IllegalArgumentException(
                    "Card id cannot be null");
        }

        BaseCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Card with id {} not found", cardId);
                    return new CardNotFoundException("Card not found + " + cardId);
                });
        cardRepository.delete(card);
    }

    /**
     * Найти карту по ID. PESSIMISTIC_WRITE
     *
     * @param cardId ID карты
     * @throws CardNotFoundException    если карта не найдена
     */
    @Transactional
    @Override
    public BaseCard findCardByIdForUpdate(Long cardId) {
        return cardRepository.findByIdForUpdate(cardId)
                .orElseThrow(() -> {
                    log.warn("Card with id {} not found", cardId);
                    return new CardNotFoundException("Card not found + " + cardId);
                });
    }

    @Override
    public BaseCard findByCardNumber(String number) {
        return (BaseCard) cardRepository.findByNumHmac(number).orElseThrow(() -> {
            log.warn("Card with number {} not found", number);
            return new CardNotFoundException("Card not found + " + number);
        });
    }
}
