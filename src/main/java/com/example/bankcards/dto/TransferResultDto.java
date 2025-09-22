package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO — результат перевода средств между картами.
 * <p>
 * Содержит обновлённые данные карт-участников после операции.
 *
 * @param source      карта-отправитель после перевода
 * @param destination карта-получатель после перевода
 */

@Schema(description = "Результат перевода средств между картами")
public record TransferResultDto(
        @Schema(description = "Карта-отправитель с обновлённым балансом")
        CardDto source,
        @Schema(description = "Карта-получатель с обновлённым балансом")
        CardDto destination
) { }