package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO для обновления пользователя.
 *
 * @param name  новое имя (опционально, может быть null)
 * @param email новый email (должен быть валидным, если передан)
 * @param role  новая роль (опционально)
 */

@Schema(description = "Данные для обновления пользователя")
public record UserUpdateDto(
        @Schema(description = "Имя пользователя", example = "Андрей")
        @Size(min = 1, max = 100, message = "Имя должно быть от 1 до 100 символов")
        String name,
        @Schema(description = "Email", example = "andrey@example.com")
        @Email(message = "Некорректный формат email")
        @Size(max = 255, message = "Email не должен превышать 255 символов")
        String email,
        @Schema(description = "Роль пользователя", example = "ADMIN, USER")
        UserRole role
) { }
