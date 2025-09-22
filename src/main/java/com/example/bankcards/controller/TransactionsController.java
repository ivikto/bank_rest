package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferResultDto;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST-контроллер для операций с транзакциями и балансом карт.
 * <p>
 * Базовый путь: {@code /api/v1/transaction}
 * <ul>
 *   <li>Перевод средств между картами</li>
 *   <li>Запрос текущего баланса по ID/номеру карты</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "API для управления транзакциями и просмотра баланса")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TransactionsController {

    private final TransferService transferService;

    /**
     * Перевод средств между картами.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Перевод средств между картами",
            description = "Выполняет перевод денежных средств между указанными картами",
            requestBody = @RequestBody(
                    description = "Параметры перевода",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TransferDto.class),
                            examples = @ExampleObject(
                                    name = "Пример перевода",
                                    value = """
                                            {
                                              "sourceCardId": 101,
                                              "destinationCardId": 202,
                                              "amount": 1500.00
                                            }"""
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransferResultDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "422", description = "Недостаточно средств на карте")
    })
    public ResponseEntity<TransferResultDto> balanceTransfer(
            @org.springframework.web.bind.annotation.RequestBody @Valid TransferDto transferDto) {
        return ResponseEntity.ok(transferService.balanceTransfer(transferDto));
    }

    /**
     * Получение текущего баланса по ID карты.
     */
    @GetMapping("/{cardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Получение баланса по ID карты",
            description = "Возвращает текущий баланс карты по её идентификатору"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BigDecimal.class),
                            examples = @ExampleObject(value = "12345.67"))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<BigDecimal> getBalanceByID(
            @Parameter(description = "ID карты", example = "1001", required = true)
            @PathVariable @Positive(message = "cardId must be > 0") Long cardId) {
        return ResponseEntity.ok(transferService.getBalance(cardId));
    }

    /**
     * Получение текущего баланса по номеру карты.
     */
    @GetMapping("/number/{cardNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Получение баланса по номеру карты",
            description = "Возвращает текущий баланс карты по её номеру (PAN)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BigDecimal.class),
                            examples = @ExampleObject(value = "9876.54"))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<BigDecimal> getBalanceByNumber(
            @Parameter(description = "Полный номер карты (PAN)", example = "2200700000001234", required = true)
            @PathVariable
            @NotBlank(message = "cardNumber must not be blank")
            @Pattern(regexp = "\\d{16}", message = "cardNumber must be exactly 16 digits")
            String cardNumber) {
        return ResponseEntity.ok(transferService.getBalance(cardNumber));
    }
}
