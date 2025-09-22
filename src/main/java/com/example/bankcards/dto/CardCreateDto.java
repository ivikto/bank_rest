package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO для создания новой банковской карты.
 * <p>
 * Содержит только идентификатор пользователя, которому будет принадлежать карта.
 *
 * @param userId идентификатор владельца карты
 */
@Schema(description = "Запрос на создание карты")
public record CardCreateDto(
        @Schema(description = "ID пользователя-владельца", example = "42")
        @NotNull(message = "userId обязателен")
        @Positive(message = "userId должен быть положительным числом")
        Long userId
) { }
