package com.example.bankcards.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Конфигурация JPA для включения механизма аудита.
 * <p>
 * Аудит позволяет автоматически заполнять технические поля
 * (например, {@code createdAt}, {@code modifiedAt}, {@code createdBy}, {@code modifiedBy})
 * в сущностях, если они аннотированы соответствующими аннотациями Spring Data JPA:
 * <ul>
 *   <li>{@code @CreatedDate}</li>
 *   <li>{@code @LastModifiedDate}</li>
 *   <li>{@code @CreatedBy}</li>
 *   <li>{@code @LastModifiedBy}</li>
 * </ul>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
