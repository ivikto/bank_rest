package com.example.bankcards.dto;

import com.example.bankcards.entity.StandardCard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO для представления банковской карты во внешнем API.
 * <p>
 * Номер карты маскируется: видны только последние 4 цифры.
 *
 * @param id         уникальный идентификатор карты
 * @param cardNumber маскированный номер карты (формат: **** **** **** 1234)
 * @param status     статус карты (например, ACTIVE, BLOCKED, EXPIRED)
 * @param balance    текущий баланс карты
 * @param expiration срок действия карты (строка, например "2025-12-31T00:00")
 */

@Schema(description = "Карта")
public record CardDto(Long id,
                      @Schema(description = "ID карты", example = "42")
                      String cardNumber,
                      @Schema(description = "Статус карты", example = "ACTIVE, BLOCKED, EXPIRED")
                      String status,
                      @Schema(description = "Баланс", example = "1000")
                      BigDecimal balance,
                      @Schema(description = "Срок действия", example = "2025-12-31T00:00")
                      String expiration) {

    /**
     * Создаёт DTO из доменной сущности {@link StandardCard}.
     * <ul>
     *   <li>Номер карты маскируется, оставляются только последние 4 цифры</li>
     *   <li>Статус конвертируется в строку</li>
     *   <li>Дата истечения приводится к строке через {@code toString()}</li>
     * </ul>
     *
     * @param c объект доменной модели {@link StandardCard}
     * @return DTO для API
     */
    public static CardDto from(StandardCard c) {
        return new CardDto(
                c.getId(),
                "**** **** **** " + c.getCardNumberLast4(),
                c.getCardStatus().name(),
                c.getBalance(),
                c.getExpiration().toString()
        );
    }
}
