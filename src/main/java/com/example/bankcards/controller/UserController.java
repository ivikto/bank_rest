package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserSearchRequestDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для операций с пользователями.
 * <p>
 * Базовый путь: {@code /api/v1/user}
 * <ul>
 *   <li>Создание пользователей</li>
 *   <li>Получение пользователя по ID</li>
 *   <li>Получение списка пользователей с фильтрацией и пагинацией</li>
 *   <li>Обновление пользователя</li>
 *   <li>Удаление пользователя</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "Операции с пользователями")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Создать нового пользователя.
     *
     * @param user DTO с данными для создания
     * @return созданный пользователь
     * <p>
     * Возможные ответы:
     * <ul>
     *   <li>200 – пользователь создан</li>
     *   <li>400 – некорректные данные</li>
     *   <li>409 – пользователь уже существует</li>
     * </ul>
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет прав", content = @Content),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует", content = @Content)
    })
    public ResponseEntity<UserDto> createUser(
            @RequestBody(description = "Данные для создания пользователя", required = true,
                    content = @Content(schema = @Schema(implementation = UserCreateDto.class)))
            @org.springframework.web.bind.annotation.RequestBody UserCreateDto user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    /**
     * Получить пользователя по его ID.
     *
     * @param userId идентификатор пользователя
     * @return DTO пользователя
     * <p>
     * Возможные ответы:
     * <ul>
     *   <li>200 – пользователь найден</li>
     *   <li>404 – пользователь не найден</li>
     * </ul>
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователя по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет прав", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<UserDto> getUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /**
     * Получить список пользователей с фильтрацией, пагинацией и сортировкой.
     *
     * @return страница с пользователями
     * <p>
     * Возможные ответы:
     * <ul>
     *   <li>200 – список получен</li>
     * </ul>
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить список пользователей",
            description = "Параметры передаются через query: page, size, sort, name, email, UserRole, isActive"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список пользователей получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет прав", content = @Content)
    })
    public ResponseEntity<Page<UserDto>> getAllUsers(
            UserSearchRequestDto request
    ) {
        Page<UserDto> result = userService.getUsers(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Удалить пользователя по его ID.
     *
     * @param userId идентификатор пользователя
     * @return пустой ответ со статусом 204
     * <p>
     * Возможные ответы:
     * <ul>
     *   <li>204 – пользователь удалён</li>
     *   <li>404 – пользователь не найден</li>
     * </ul>
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет прав", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Обновить данные пользователя по его ID.
     *
     * @param userId        идентификатор пользователя
     * @param userUpdateDto DTO с обновлёнными данными
     * @return обновлённый пользователь
     * <p>
     * Возможные ответы:
     * <ul>
     *   <li>200 – пользователь обновлён</li>
     *   <li>400 – некорректные данные</li>
     *   <li>404 – пользователь не найден</li>
     * </ul>
     */
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет прав", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId,
            @RequestBody(description = "Данные для обновления", required = true,
                    content = @Content(schema = @Schema(implementation = UserUpdateDto.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateUser(userId, userUpdateDto));
    }
}

