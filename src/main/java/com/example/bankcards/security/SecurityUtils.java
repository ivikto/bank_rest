package com.example.bankcards.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Утилита для работы с текущей аутентификацией Spring Security.
 * <p>
 * Предоставляет методы для получения:
 * - идентификатора текущего пользователя,
 * - email (username) из Authentication,
 * - ролей/полномочий,
 * - признака, является ли текущий пользователь администратором.
 * <p>
 * Генерирует AccessDeniedException, если запрос выполняется без аутентификации.
 */
@Component
public class TransferPolicy {

    /**
     * Возвращает текущий объект аутентификации из SecurityContext.
     *
     * @return текущая {@link Authentication}
     * @throws AccessDeniedException если контекст пуст или пользователь не аутентифицирован
     */
    private Authentication auth() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated");
        }
        return a;
    }

    /**
     * Возвращает идентификатор текущего пользователя из principal.
     *
     * @return ID пользователя
     * @throws AccessDeniedException   если пользователь не аутентифицирован
     * @throws IllegalStateException   если principal не содержит идентификатор
     */
    public Long currentUserId() {
        Object p = auth().getPrincipal();
        if (p instanceof AppUserPrincipal me) return me.getId();
        throw new IllegalStateException("Principal does not expose id");
    }

    /**
     * Возвращает имя (email) текущего пользователя.
     *
     * @return email/username из {@link Authentication#getName()}
     * @throws AccessDeniedException если пользователь не аутентифицирован
     */
    public String currentEmail() {
        return auth().getName();
    }

    /**
     * Проверяет, имеет ли текущий пользователь роль администратора.
     *
     * @return true, если присутствует роль ROLE_ADMIN
     * @throws AccessDeniedException если пользователь не аутентифицирован
     */
    public boolean isAdmin() {
        return authorities().stream().anyMatch(ga -> "ROLE_ADMIN".equals(ga.getAuthority()));
    }

    /**
     * Возвращает коллекцию полномочий текущего пользователя.
     *
     * @return коллекция {@link GrantedAuthority}
     * @throws AccessDeniedException если пользователь не аутентифицирован
     */
    public Collection<? extends GrantedAuthority> authorities() {
        return auth().getAuthorities();
    }
}
