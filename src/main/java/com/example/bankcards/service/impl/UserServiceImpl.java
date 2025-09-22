package com.example.bankcards.service.impl;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserSearchRequestDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.exception.DuplicateUserException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.factory.UserFactory;
import com.example.bankcards.mapper.BankMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.spec.UserSpecs;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.PageableBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * Сервис управления пользователями.
 * <p>
 * Функциональность:
 * <ul>
 *   <li>Создание пользователя с валидацией и обработкой уникальности email</li>
 *   <li>Получение пользователя по ID (включая связанные карты)</li>
 *   <li>Поиск пользователей с пагинацией/сортировкой и фильтрами (имя, email, роль, активность)</li>
 *   <li>Частичное обновление данных пользователя</li>
 *   <li>Удаление пользователя</li>
 * </ul>
 * Все методы используют транзакции Spring и логируют нарушения валидации/ошибки.
 * Зависимости:
 * <ul>
 *   <li>{@link UserRepository} — доступ к данным пользователей</li>
 *   <li>{@link BankMapper} — маппинг сущностей в DTO</li>
 *   <li>{@link UserFactory} — фабрика создания доменной сущности из DTO</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BankMapper mapper;
    private final UserFactory userFactory;

    /**
     * Создает нового пользователя.
     * <p>
     * Валидирует имя и обрабатывает конфликт email (уникальность).
     *
     * @param userCreateDto данные для создания
     * @return созданный пользователь в виде DTO
     * @throws IllegalArgumentException при пустом имени
     * @throws DuplicateUserException   при конфликте email (существует пользователь с таким же email)
     */
    @Override
    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {

        if (userCreateDto == null) {
            log.warn("Validation failed: userCreateDto must not be null");
            throw new IllegalArgumentException("UserCreateDto must not be null");
        }

        if (userCreateDto.name().isBlank()) {
            log.warn("Validation failed: User name must not be empty");
            throw new IllegalArgumentException("User name must not be empty");
        }

        BaseUser user = userFactory.createUser(userCreateDto);

        try {
            user = userRepository.save(user);
            return mapper.userToUserDTO(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Validation failed: User with same email already exists: {}", user.getEmail());
            throw new DuplicateUserException("User with same email already exists", e);
        }
    }

    /**
     * Возвращает пользователя по ID вместе с его картами.
     *
     * @param userId идентификатор пользователя
     * @return DTO пользователя
     * @throws java.util.NoSuchElementException если пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(Long userId) {
        if (userId == null) {
            log.warn("Validation failed: userID must not be null");
            throw new IllegalArgumentException("userID must not be null");
        }

        BaseUser user = userRepository.findByIdWithCards(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new UserNotFoundException("User not found");
        });
        return mapper.userToUserDTO(user);
    }

    /**
     * Возвращает страницу пользователей по критериям поиска.
     * <p>
     * Поддерживаются пагинация, сортировка и фильтры (имя, email, роль, активность).
     *
     * Правила валидации:
     * <ul>
     *   <li>page >= 0</li>
     *   <li>size ∈ [1..100]</li>
     *   <li>sort — формат совместим с Pageable (например: {@code field,asc|desc})</li>
     * </ul>
     *
     * @param dto параметры поиска и пагинации:
     *            page, size, sort, name, email, role, isActive
     * @return страница DTO пользователей
     * @throws IllegalArgumentException при некорректных значениях page/size или ошибке парсинга сортировки
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(UserSearchRequestDto dto) {

        if (dto.size() > 100) {
            log.warn("Validation failed: size must be in [1,100]");
            throw new IllegalArgumentException("size must be in [1,100]");
        }

        Pageable pageable = null;
        try {
            pageable = PageableBuilder.build(dto.page(), dto.size(), dto.sort());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Wrong filter parameters");
        }

        Specification<BaseUser> spec = Specification.allOf(
                UserSpecs.nameContains(dto.name()),
                UserSpecs.emailContains(dto.email()),
                UserSpecs.roleEq(dto.role()),
                UserSpecs.isActiveEq(dto.isActive())
        );
        return userRepository.findAll(spec, pageable).map(mapper::userToUserDTO);
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId идентификатор пользователя
     * @throws IllegalArgumentException если {@code userId} равен null
     * @throws UserNotFoundException    если пользователь не найден
     */
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (userId == null) {
            log.warn("Validation failed: User id cannot be null");
            throw new IllegalArgumentException("User id cannot be null");
        }

        BaseUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found + " + userId));

        userRepository.delete(user);

    }

    /**
     * Частично обновляет данные пользователя.
     * <p>
     * Обновляет имя, email и роль. Поле modifiedAt устанавливается текущим временем.
     * Обрабатывает конфликт email.
     *
     * @param userId        идентификатор пользователя
     * @param userUpdateDto данные для обновления
     * @return обновленный пользователь в виде DTO
     * @throws UserNotFoundException  если пользователь не найден
     * @throws DuplicateUserException при конфликте email
     */
    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        if (userId == null || userUpdateDto == null) {
            log.warn("Validation failed: User id and userUpdateDto cannot be null");
            throw new IllegalArgumentException("User id and userUpdateDto cannot be null");
        }

        BaseUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                            log.warn("User with id {} not found", userId);
                            return new UserNotFoundException("User not found + " + userId);
                        }
                );

        if (userUpdateDto.name() != null) {
            user.setName(userUpdateDto.name());
        }
        if (userUpdateDto.email() != null) {
            user.setEmail(userUpdateDto.email());
        }
        if (userUpdateDto.role() != null) {
            user.setRole(userUpdateDto.role());
        }

        user.setModifiedAt(LocalDateTime.now());

        try {
            user = userRepository.save(user);
            return mapper.userToUserDTO(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Validation failed: User with same email already exists: {}", user.getEmail());
            throw new DuplicateUserException("User with same email already exists", e);
        }
    }
}
