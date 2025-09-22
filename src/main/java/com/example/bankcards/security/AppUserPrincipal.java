package com.example.bankcards.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Адаптер доменной модели пользователя к контракту Spring Security {@link UserDetails}.
 *
 * <p>Содержит минимально необходимый набор полей для аутентификации и авторизации:
 * идентификатор пользователя, e-mail как логин, хэш пароля, флаг активности и коллекцию прав.
 *
 * <p>Поведение:
 * <ul>
 *   <li>{@link #getUsername()} возвращает e-mail;</li>
 *   <li>{@link #isEnabled()} и {@link #isAccountNonLocked()} зависят от флага {@code active};</li>
 *   <li>Срок действия учётной записи и учётных данных не ограничен ({@code true}).</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public class AppUserPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Имя пользователя для аутентификации.
     *
     * @return e-mail (логин)
     */
    @Override public String getUsername() { return email; }

    /**
     * Признак включённости учётной записи.
     *
     * @return {@code true}, если пользователь активен
     */
    @Override public boolean isEnabled() { return active; }

    /**
     * Признак, что учётная запись не просрочена.
     *
     * @return всегда {@code true} (срок действия не ограничен)
     */
    @Override public boolean isAccountNonExpired() { return true; }

    /**
     * Признак незаблокированной учётной записи.
     *
     * @return {@code true}, если пользователь активен
     */
    @Override public boolean isAccountNonLocked() { return active; }

    /**
     * Признак, что учётные данные не просрочены.
     *
     * @return всегда {@code true} (срок действия пароля не отслеживается)
     */
    @Override public boolean isCredentialsNonExpired() { return true; }
}
