package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO для передачи информации об ошибке в REST API.
 * <p>
 * Используется в качестве унифицированного ответа при исключениях.
 *
 * @param timestamp момент возникновения ошибки
 * @param status    HTTP-статус код (например, 400, 404, 500)
 * @param error     краткое название ошибки (например, "Bad Request")
 * @param message   описание причины ошибки
 * @param path      URI запроса, в котором произошла ошибка
 */
@Schema(description = "Структура ответа об ошибке")
public record ErrorResponseDto(
        @Schema(description = "Момент возникновения ошибки", example = "2025-09-21T15:34:12")
        LocalDateTime timestamp,
        @Schema(description = "HTTP-статус код", example = "404")
        int status,
        @Schema(description = "Название ошибки", example = "Not Found")
        String error,
        @Schema(description = "Сообщение об ошибке", example = "Карта с указанным ID не найдена")
        String message,
        @Schema(description = "Путь запроса", example = "/api/v1/card/123")
        String path
) { }
