package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserSearchRequestDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;

/**
 * Сервис работы с пользователями.
 *
 * <p>Общие правила:
 * <ul>
 *   <li>Методы чтения выполняются с {@code readOnly} транзакциями.</li>
 *   <li>Операции изменения выполняются в обычной транзакции.</li>
 *   <li>Проверка прав — на уровне контроллера/методовой безопасности.</li>
 * </ul>
 */
@Validated
public interface UserService {

    /**
     * Создаёт нового пользователя.
     *
     * <p>Должен проверять уникальность e-mail/логина согласно бизнес-правилам.
     *
     * @param user данные для создания (не {@code null})
     * @return созданный пользователь
     * @throws IllegalArgumentException если {@code user} некорректен
     * @throws org.springframework.dao.DataIntegrityViolationException при нарушении уникальных ограничений
     */
    UserDto createUser(@Valid UserCreateDto user);

    /**
     * Возвращает пользователя по ID.
     *
     * @param userId идентификатор (не {@code null}, &gt; 0)
     * @return DTO пользователя
     * @throws IllegalArgumentException если {@code userId} некорректен
     * @throws UserNotFoundException если пользователь не найден
     */
    UserDto getUser(@NotNull @Positive Long userId);

    /**
     * Возвращает страницу пользователей по фильтрам/сортировке.
     *
     * <p>Требования к полям запроса:
     * <ul>
     *   <li>{@code page} ≥ 0, {@code size} в диапазоне [1..100];</li>
     *   <li>{@code sort} — формат {@code field,asc|desc};</li>
     *   <li>остальные фильтры — опциональны (name/email частичное совпадение, role={@link UserRole}, isActive).</li>
     * </ul>
     *
     * @param userSearchRequestDto параметры поиска (не {@code null})
     * @return страница пользователей
     * @throws IllegalArgumentException при нарушении правил валидации запроса
     */
    Page<UserDto> getUsers(@Valid UserSearchRequestDto userSearchRequestDto);

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId идентификатор (не {@code null}, &gt; 0)
     * @throws IllegalArgumentException если {@code userId} некорректен
     * @throws UserNotFoundException если пользователь не найден
     */
    void deleteUser(@NotNull @Positive Long userId);

    /**
     * Частично обновляет пользователя.
     *
     * @param userId идентификатор (не {@code null}, &gt; 0)
     * @param userUpdateDto данные для обновления (не {@code null})
     * @return обновлённый пользователь
     * @throws IllegalArgumentException если входные данные некорректны
     * @throws UserNotFoundException если пользователь не найден
     */
    UserDto updateUser(@NotNull @Positive Long userId, @Valid  UserUpdateDto userUpdateDto);
}
