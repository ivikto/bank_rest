package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

/**
 * DTO для частичного обновления карты.
 * <p>
 * Может использоваться для изменения владельца карты или её статуса.
 *
 * @param cardId идентификатор карты, которую нужно обновить
 * @param userId идентификатор нового владельца (опционально)
 * @param status новый статус карты (например, ACTIVE, BLOCKED, EXPIRED)
 */

@Schema(description = "Данные для обновления информации о карте")
public record CardUpdateDto(
        @Schema(description = "ID карты", example = "1")
        @Positive
        Long cardId,
        @Schema(description = "ID пользователя", example = "1")
        @Positive
        Long userId,
        CardStatus status) {
}
