package com.example.bankcards.controller;

import com.example.bankcards.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для операций аутентификации пользователей.
 * <p>
 * Базовый путь: {@code /api/v1/auth}
 * <ul>
 *   <li>Вход по email и паролю</li>
 *   <li>Получение JWT для дальнейшего доступа к защищённым ресурсам</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и получения JWT")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService uds;

    /**
     * DTO для передачи учётных данных пользователя при входе.
     *
     * @param email    email (используется как username)
     * @param password пароль в открытом виде
     */
    @Schema(description = "Запрос на вход")
    public record LoginRequest(
            @Schema(description = "Email пользователя", example = "ivan@example.com")
            String email,

            @Schema(description = "Пароль пользователя", example = "P@ssw0rd123")
            String password
    ) {}

    /**
     * DTO с ответом при успешной аутентификации.
     *
     * @param token подписанный JWT, используемый в заголовке Authorization
     */
    @Schema(description = "Ответ с JWT-токеном")
    public record TokenResponse(
            @Schema(description = "JWT-токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            String token
    ) {}

    /**
     * Выполняет вход пользователя по email и паролю.
     * <p>
     * При успешной аутентификации создаётся и возвращается JWT-токен,
     * который можно использовать для авторизации в других эндпоинтах API.
     *
     * @param r объект с email и паролем
     * @return JWT-токен, обёрнутый в {@link TokenResponse}
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему",
            description = "Аутентифицирует пользователя по email и паролю и возвращает JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Аутентификация успешна",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учётные данные", content = @Content)
    })
    public ResponseEntity<TokenResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody LoginRequest r
    ) {
        var auth = new UsernamePasswordAuthenticationToken(r.email(), r.password());
        authManager.authenticate(auth);
        var user = uds.loadUserByUsername(r.email());
        return ResponseEntity.ok(new TokenResponse(jwtService.generateToken(user)));
    }
}
