package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequestDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST-контроллер для работы с банковскими картами.
 * <p>
 * Базовый путь: {@code /api/v1/card}
 * <ul>
 *   <li>Создание карт (администратор)</li>
 *   <li>Получение списка карт с фильтрами и пагинацией</li>
 *   <li>Получение карты по ID или номеру</li>
 *   <li>Частичное обновление данных карты</li>
 *   <li>Удаление карты</li>
 * </ul>
 */

@RestController
@RequestMapping("/api/v1/card")
@RequiredArgsConstructor
@Validated
@Tag(name = "Cards", description = "Операции с картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать карту", description = "Создает карту для указанного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта создана",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет прав", content = @Content)
    })

    public ResponseEntity<CardDto> createCard(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания карты",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CardCreateDto.class)))
            @org.springframework.web.bind.annotation.RequestBody @Valid CardCreateDto cardCreateDto) {
        return ResponseEntity.ok(cardService.createCard(cardCreateDto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(
            summary = "Получить список карт",
            description = "Параметры передаются через query: page, size, sort, userId, last4, status, expirationFrom, expirationTo, balanceMin, balanceMax, createdFrom, createdTo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список карт получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав")
    })
    public ResponseEntity<Page<CardDto>> getAllCards(CardSearchRequestDto request
    ) {
        Page<CardDto> result = cardService.getCards(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Получить карту по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<CardDto> getCard(
            @Parameter(description = "ID карты", example = "1001", required = true)
            @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @GetMapping("/number/{cardNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Получить карту по номеру")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<CardDto> getCard(
            @Parameter(description = "Полный номер карты (PAN)", example = "2200700000001234", required = true)
            @PathVariable String cardNumber) {
        return ResponseEntity.ok(cardService.getCard(cardNumber));
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Частичное обновление карты")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта обновлена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<CardDto> updateCard(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления карты",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CardUpdateDto.class)))
            @org.springframework.web.bind.annotation.RequestBody CardUpdateDto cardUpdateDto) {
        return ResponseEntity.ok(cardService.updateCard(cardUpdateDto));
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты", example = "1001", required = true)
            @PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}


