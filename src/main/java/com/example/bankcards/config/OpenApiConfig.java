package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI/Swagger для проекта.
 * <p>
 * Определяет общие метаданные API и схему безопасности JWT.
 * После старта приложения спецификация будет доступна по стандартным
 * путям Swagger UI и OpenAPI (например, {@code /swagger-ui.html}, {@code /v3/api-docs}).
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создаёт объект OpenAPI с основной информацией о сервисе и схемой безопасности.
     * <ul>
     *   <li>Название: {@code Bank_rest API}</li>
     *   <li>Описание: {@code REST сервис для управления картами/пользователями}</li>
     *   <li>Версия: v1</li>
     *   <li>Схема безопасности: {@code bearerAuth} (JWT в заголовке Authorization)</li>
     * </ul>
     *
     * @return сконфигурированный {@link OpenAPI} объект
     */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank_rest API")
                        .description("REST сервис для управления картами/пользователями")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
