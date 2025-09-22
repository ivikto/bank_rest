package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequestDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;

/**
 * Работа с банковскими картами.
 *
 * <p>Правила доступа:
 * <ul>
 *   <li>Админ — полные права;</li>
 *   <li>Пользователь — только свои карты.</li>
 * </ul>
 */
@Validated
public interface CardService {

    /**
     * Создаёт карту для пользователя.
     *
     * <p>Делает до 3 попыток сохранения при коллизиях уникальных полей.
     *
     * @param cardCreateDto содержит идентификатор владельца (должен быть &gt; 0)
     * @return созданная карта
     * @throws IllegalArgumentException если {@code userId} == 0
     * @throws UserNotFoundException если пользователь не найден
     * @throws org.springframework.dao.DataIntegrityViolationException если все попытки сохранения неудачны
     */
    CardDto createCard(CardCreateDto cardCreateDto);

    /**
     * Возвращает страницу карт по фильтрам/пагинации.
     *
     * <p>Если текущий пользователь не админ, выборка ограничивается его картами.
     *
     * @param dto параметры поиска (page ≥ 0, 1 ≤ size ≤ 100, last4 = 4 цифры и т.д.)
     * @return страница DTO
     * @throws IllegalArgumentException при некорректных параметрах или {@code dto == null}
     */
    Page<CardDto> getCards(@Valid CardSearchRequestDto dto);

    /**
     * Получает карту по ID. Доступ — только владельцу или админу.
     *
     * @param cardId ID карты (не null)
     * @return DTO карты
     * @throws IllegalArgumentException если {@code cardId} == null
     * @throws CardNotFoundException если карта не найдена
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не владелец
     */
    CardDto getCard(Long cardId);

    /** См. {@link #getCard(Long)} но возвращает сущность. */
    BaseCard getBaseCard(@NotNull @Positive Long cardId);

    /**
     * Получает карту по номеру (PAN).
     *
     * <p>PAN валидируется и преобразуется в HMAC; поиск выполняется по HMAC.
     *
     * @param cardNumber полный номер карты (PAN)
     * @return DTO карты
     * @throws IllegalArgumentException при {@code cardNumber == null} или неверном формате
     * @throws CardNotFoundException если карта не найдена
     * @throws org.springframework.security.access.AccessDeniedException если текущий пользователь не владелец
     */
    CardDto getCard(@NotBlank String cardNumber);

    /**
     * Частично обновляет статус и/или владельца карты под блокировкой строки.
     *
     * @param cardUpdateDto данные обновления (cardId обязателен)
     * @return обновлённая карта
     * @throws IllegalArgumentException при некорректных данных
     * @throws CardNotFoundException если карта не найдена
     * @throws UserNotFoundException если новый владелец не найден
     */
    CardDto updateCard(@Valid CardUpdateDto cardUpdateDto);

    /**
     * Удаляет карту по ID.
     *
     * @param cardId ID карты
     * @throws IllegalArgumentException если {@code cardId} == null
     * @throws CardNotFoundException если карта не найдена
     */
    void deleteCard(@NotNull @Positive Long cardId);

    /**
     * Находит карту по ID с блокировкой PESSIMISTIC_WRITE.
     *
     * @param cardId ID карты
     * @return сущность карты
     * @throws CardNotFoundException если карта не найдена
     */
    BaseCard findCardByIdForUpdate(@NotNull @Positive Long cardId);

    /**
     * Находит карту по HMAC номера.
     *
     * @param number HMAC PAN (не сам PAN)
     * @return сущность карты
     * @throws CardNotFoundException если не найдена
     */
    BaseCard findByCardNumber(@NotBlank String number);
}