package com.example.bankcards.service;

import com.example.bankcards.TestUtils;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.IdenticalCardsException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.TransferAccessException;
import com.example.bankcards.mapper.BankMapper;
import com.example.bankcards.security.SecurityUtils;
import com.example.bankcards.service.impl.TransferServiceImpl;
import com.example.bankcards.util.CardNumberCheck;
import com.example.bankcards.util.CardsPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private CardService cardService;
    @Mock
    private BankMapper mapper;
    @Mock
    private CardNumberCheck cardNumberCheck;
    @Mock
    CardsPolicy cardsPolicy;
    @Mock
    SecurityUtils securityUtils;

    private final TestUtils testUtils = new TestUtils();

    @InjectMocks
    private TransferServiceImpl  transferService;


    @Test
    void transferSomeSourceAndDestTest() {
        when(securityUtils.currentUserId()).thenReturn(1L);
        TransferDto dto = testUtils.getTransferDto(1L, 1L, new BigDecimal("1000"));

        doThrow(new IdenticalCardsException("same"))
                .when(cardsPolicy).assertNotSameCards(dto);

        assertThrows(IdenticalCardsException.class,
                () -> transferService.balanceTransfer(dto));

        verify(cardsPolicy).assertNotSameCards(dto);
        verifyNoInteractions(cardService, mapper);
    }

    @Test
    void transferCardNotFoundOneTest() {
        TransferDto dto = testUtils.getTransferDto(1L, 2L, new BigDecimal("1000"));

        when(cardService.findCardByIdForUpdate(dto.sourceCardId())).thenThrow(new CardNotFoundException("some"));

        assertThrows(CardNotFoundException.class,
                () -> transferService.balanceTransfer(dto));

        verifyNoInteractions(cardNumberCheck, mapper);
    }

    @Test
    void transferCardNotFoundTwoTest() {

        TransferDto dto = testUtils.getTransferDto(1L, 2L, new BigDecimal("1000"));
        BaseCard sourceCard = testUtils.getBaseCard(dto.sourceCardId());

        when(cardService.findCardByIdForUpdate(dto.sourceCardId())).thenReturn(sourceCard);
        when(cardService.findCardByIdForUpdate(dto.destinationCardId())).thenThrow(new CardNotFoundException("some"));

        assertThrows(CardNotFoundException.class,
                () -> transferService.balanceTransfer(dto));

        verifyNoInteractions(cardNumberCheck, mapper);
    }

    @Test
    void transferOwnByUserTest() {
        TransferDto dto = testUtils.getTransferDto(1L, 2L, new BigDecimal("1000"));
        BaseCard src = testUtils.getBaseCard(dto.sourceCardId());
        BaseCard dst = testUtils.getBaseCard(dto.destinationCardId());

        when(securityUtils.currentUserId()).thenReturn(1L);
        when(cardService.findCardByIdForUpdate(dto.sourceCardId())).thenReturn(src);
        when(cardService.findCardByIdForUpdate(dto.destinationCardId())).thenReturn(dst);

        doThrow(new TransferAccessException("come"))
                .when(cardsPolicy)
                .assertOwnedByUser(
                        argThat(c -> Objects.equals(c.getId(), dto.sourceCardId())),
                        argThat(c -> Objects.equals(c.getId(), dto.destinationCardId()))
                );

        assertThrows(TransferAccessException.class, () -> transferService.balanceTransfer(dto));
    }

    @Test
    void transferCardActiveTest() {
        TransferDto dto = testUtils.getTransferDto(1L, 2L, new BigDecimal("1000"));
        BaseCard src = testUtils.getBaseCard(dto.sourceCardId());
        BaseCard dst = testUtils.getBaseCard(dto.destinationCardId());

        when(securityUtils.currentUserId()).thenReturn(1L);
        when(cardService.findCardByIdForUpdate(dto.sourceCardId())).thenReturn(src);
        when(cardService.findCardByIdForUpdate(dto.destinationCardId())).thenReturn(dst);
        doThrow(new TransferAccessException("come"))
                .when(cardsPolicy)
                .assertActive(argThat(c -> c.getId().equals(dto.sourceCardId())));


        assertThrows(TransferAccessException.class, () -> transferService.balanceTransfer(dto));
    }

    @Test
    void transferBalanceTest() {
        TransferDto dto = testUtils.getTransferDto(1L, 2L, new BigDecimal("1"));
        BaseCard src = testUtils.getBaseCard(dto.sourceCardId());
        src.setBalance(new BigDecimal("1000"));
        BaseCard dst = testUtils.getBaseCard(dto.destinationCardId());
        dst.setBalance(new BigDecimal("0"));

        when(securityUtils.currentUserId()).thenReturn(1L);
        when(cardService.findCardByIdForUpdate(dto.sourceCardId())).thenReturn(src);
        when(cardService.findCardByIdForUpdate(dto.destinationCardId())).thenReturn(dst);
        doThrow(new InsufficientFundsException("come"))
                .when(cardsPolicy)
                .assertInsufficientFunds(dto.amount(), src);

        assertThrows(InsufficientFundsException.class, () -> transferService.balanceTransfer(dto));
    }

    @Test
    void transferNegativeBalanceTest() {
        var dto = testUtils.getTransferDto(1L, 2L, new BigDecimal("1001"));
        var src = testUtils.getBaseCard(dto.sourceCardId());
        var dst = testUtils.getBaseCard(dto.destinationCardId());
        src.setBalance(new BigDecimal("1000"));
        dst.setBalance(BigDecimal.ZERO);

        when(securityUtils.currentUserId()).thenReturn(1L);
        when(cardService.findCardByIdForUpdate(dto.sourceCardId())).thenReturn(src);
        when(cardService.findCardByIdForUpdate(dto.destinationCardId())).thenReturn(dst);

        doCallRealMethod().when(cardsPolicy).assertInsufficientFunds(any(), any());

        assertThrows(InsufficientFundsException.class, () -> transferService.balanceTransfer(dto));
    }

    @Test
    void transferPositiveBalanceTest() {
        var dto = testUtils.getTransferDto(1L, 2L, new BigDecimal("1"));
        var src = testUtils.getBaseCard(dto.sourceCardId());
        var dst = testUtils.getBaseCard(dto.destinationCardId());
        src.setBalance(new BigDecimal("1000"));
        dst.setBalance(BigDecimal.ZERO);

        when(securityUtils.currentUserId()).thenReturn(1L);
        when(cardService.findCardByIdForUpdate(dto.sourceCardId())).thenReturn(src);
        when(cardService.findCardByIdForUpdate(dto.destinationCardId())).thenReturn(dst);

        doCallRealMethod().when(cardsPolicy).assertInsufficientFunds(any(), any());

        assertDoesNotThrow(() -> transferService.balanceTransfer(dto));
    }




}
