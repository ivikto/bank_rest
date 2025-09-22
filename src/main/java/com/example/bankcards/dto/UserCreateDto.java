package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import com.example.bankcards.entity.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для создания нового пользователя.
 *
 * @param name     имя пользователя (обязательное, непустое)
 * @param email    email пользователя (должен быть уникальным и валидным)
 * @param role     роль пользователя (например, ADMIN или USER)
 * @param userType тип пользователя (например, INTERNAL или EXTERNAL)
 * @param password пароль (хранится в захэшированном виде)
 */

@Schema(description = "Запрос на создание нового пользователя")
public record UserCreateDto(
        @Schema(description = "Имя пользователя", example = "Иван Иванов")
        @NotBlank(message = "Имя не может быть пустым")
        String name,
        @Schema(description = "Email пользователя", example = "ivan@example.com")
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        String email,
        @Schema(description = "Роль пользователя", example = "ADMIN")
        UserRole role,
        @Schema(description = "Тип пользователя", example = "INTERNAL")
        UserType userType,
        @Schema(description = "Пароль (будет захэширован)", example = "P@ssw0rd123")
        String password
) { }
