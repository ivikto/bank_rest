package com.example.bankcards.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Утилитный класс для создания объектов {@link Pageable}.
 * <p>
 * Позволяет безопасно формировать параметры пагинации и сортировки
 * с ограничением на допустимые поля сортировки и максимальный размер страницы.
 */
@UtilityClass
public class PageableBuilder {

    /**
     * Максимально допустимое количество элементов на странице.
     * <p>
     * Если запрошенный {@code size} превышает это значение,
     * то будет использовано {@code MAX_SIZE}.
     */
    private static final int MAX_SIZE = 100;

    /**
     * Список разрешённых полей для сортировки пользователей.
     */
    private static final Set<String> USER_SORT_WHITELIST =
            Set.of("id", "name", "email", "role", "isActive", "createdAt", "modifiedAt");

    /**
     * Создаёт объект {@link Pageable} для использования в Spring Data JPA.
     * <p>
     * Формат параметра {@code sort}: {@code field,direction}, где:
     * <ul>
     *   <li>{@code field} — одно из разрешённых полей сортировки</li>
     *   <li>{@code direction} — направление сортировки: {@code asc} или {@code desc}</li>
     * </ul>
     * <p>
     * Если параметр {@code size} превышает {@link #MAX_SIZE},
     * то будет подставлено значение {@link #MAX_SIZE}.
     *
     * @param page номер страницы (начиная с 0)
     * @param size количество элементов на странице (ограничивается {@link #MAX_SIZE})
     * @param sort строка с полем и направлением сортировки
     * @return объект {@link Pageable} с заданными параметрами
     * @throws IllegalArgumentException если поле сортировки не разрешено
     *                                  или формат параметра {@code sort} некорректен
     */
    public static Pageable build(int page, int size, String sort) {
        String[] parts = sort.split(",", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Неверный формат sort: field,asc|desc");
        }

        String field = parts[0];
        String dir = parts[1].toLowerCase();

        if (!USER_SORT_WHITELIST.contains(field)) {
            throw new IllegalArgumentException("Сортировка по полю не разрешена: " + field);
        }

        int pageSize = Math.min(size, MAX_SIZE);
        Sort.Direction direction = dir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, pageSize, Sort.by(direction, field));
    }
}