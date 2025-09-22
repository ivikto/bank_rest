package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для представления пользователя во внешнем API.
 *
 * Содержит основную информацию о пользователе и его картах.
 */

@Schema(description = "Пользователь")
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserDto {

    @Schema(description = "ID пользователя", example = "42")
    public long id;
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    public String name;
    @Schema(description = "Email пользователя", example = "ivan@example.com")
    public String email;
    @Schema(description = "Привязанные карты пользователя")
    public List<CardDto> cards;
    @Schema(description = "Роль пользователя", example = "ADMIN")
    public UserRole role;
    @Schema(description = "Флаг активности", example = "true")
    public boolean isActive;
    @Schema(description = "Дата и время создания", example = "2025-09-21T15:45:00")
    public LocalDateTime createdAt;
}
