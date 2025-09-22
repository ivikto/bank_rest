package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "CardSearchRequest", description = "Параметры пагинации, сортировки и фильтрации списка карт")
public record CardSearchRequestDto(
        @Schema(description = "Номер страницы (начиная с 0)", example = "0", minimum = "0", defaultValue = "0")
        Integer page,

        @Schema(description = "Размер страницы (1..100)", example = "20", minimum = "1", maximum = "100", defaultValue = "20")
        Integer size,

        @Schema(description = "Сортировка 'field,asc|desc;field2,asc|desc'", example = "createdAt,desc;id,asc")
        String sort,

        @Schema(description = "ID владельца (для админа)", example = "123")
        Long userId,

        @Schema(description = "Последние 4 цифры", example = "1234", pattern = "\\d{4}")
        String last4,

        @Schema(description = "Статус", example = "ACTIVE",
                allowableValues = {"ACTIVE", "BLOCKED", "EXPIRED", "INACTIVE"})
        CardStatus status,

        @Schema(description = "Срок действия: от (включительно), ISO-8601", example = "2025-01-01T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime expirationFrom,

        @Schema(description = "Срок действия: до (включительно), ISO-8601", example = "2026-12-31T23:59:59")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime expirationTo,

        @Schema(description = "Минимальный баланс", example = "0.00")
        BigDecimal balanceMin,

        @Schema(description = "Максимальный баланс", example = "100000.00")
        BigDecimal balanceMax,

        @Schema(description = "Дата создания: с (включительно), ISO-8601", example = "2024-01-01T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdFrom,

        @Schema(description = "Дата создания: по (включительно), ISO-8601", example = "2024-12-31T23:59:59")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdTo
) {
    public CardSearchRequestDto {
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size < 1) ? 20 : Math.min(size, 100);
    }
}
