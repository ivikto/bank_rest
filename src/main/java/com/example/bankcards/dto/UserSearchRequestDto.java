package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;


@ParameterObject
@Schema(name = "UserSearchRequestDto", description = "Параметры пагинации, сортировки и фильтрации списка пользователей")
public record UserSearchRequestDto(
        @Schema(description = "Номер страницы (0..N)", example = "0", minimum = "0", defaultValue = "0")
        Integer page,
        @Schema(description = "Размер страницы (1..100)", example = "20", minimum = "1", maximum = "100", defaultValue = "20")
        Integer size,
        @Schema(description = "Сортировка, формат: field,asc|desc", example = "createdAt,desc")
        String sort,
        @Schema(description = "Фильтр по имени (подстрока, регистр нечувствителен)", example = "Иван")
        String name,
        @Schema(description = "Фильтр по email (полное или частичное совпадение)", example = "ivan@example.com")
        String email,
        @Schema(description = "Фильтр по роли пользователя", example = "USER", implementation = UserRole.class)
        UserRole role,
        @Schema(description = "Фильтр по активности пользователя", example = "true")
        Boolean isActive
) {
    public UserSearchRequestDto {
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size < 1) ? 20 : Math.min(size, 100);
        sort = (sort == null || sort.isBlank()) ? "createdAt,desc" : sort;
    }
}
