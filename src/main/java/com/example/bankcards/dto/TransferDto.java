package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO для запроса перевода средств между картами.
 *
 * @param sourceCardId      идентификатор карты-отправителя
 * @param destinationCardId идентификатор карты-получателя
 * @param amount            сумма перевода (положительное значение)
 */

@Schema(description = "Запрос на перевод средств")
public record TransferDto(
        @Schema(description = "ID карты источника перевода", example = "1")
        @Positive(message = "Не может быть пустым")
        Long sourceCardId,
        @Schema(description = "ID карты назначения перевода", example = "2")
        @Positive(message = "Не может быть пустым")
        Long destinationCardId,
        @Schema(description = "Количество денежных средств для перевода", example = "1000")
        @Positive(message = "Не может быть 0 или отрицательным")
        @NotNull
        BigDecimal amount) {
}
