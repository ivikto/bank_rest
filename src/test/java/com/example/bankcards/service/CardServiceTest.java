package com.example.bankcards.service;

import com.example.bankcards.TestUtils;
import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequestDto;
import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.factory.CardFactory;
import com.example.bankcards.mapper.BankMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.SecurityUtils;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.CardNumberCheck;
import com.example.bankcards.util.CardsSearchFilterPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardFactory cardFactory;
    @Mock
    private BankMapper mapper;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private CardNumberCheck cardNumberCheck;
    @Mock
    private CardsSearchFilterPolicy searchPolicy;


    private final TestUtils testUtils = new TestUtils();

    @InjectMocks
    private CardServiceImpl service;

    @Test
    void createCardSuccessTest() {
        long uid = 10L;



        CardCreateDto cardCreateDto = testUtils.getCardCreateDto(uid);
        BaseUser user = testUtils.getBaseUser();
        user.setId(uid);
        BaseCard newCard = testUtils.getBaseCard(100L);
        newCard.setUser(user);
        newCard.setCardStatus(CardStatus.ACTIVE);

        when(userRepository.findById(uid)).thenReturn(Optional.of(user));
        when(cardFactory.createCard(user)).thenReturn(newCard);
        when(cardRepository.saveAndFlush(newCard)).thenReturn(newCard);
        when(mapper.cardToCardDto(newCard)).thenReturn(testUtils.getCardDto());

        CardDto result = service.createCard(cardCreateDto);

        assertThat(result).isNotNull();
        verify(cardRepository).saveAndFlush(newCard);
        verify(mapper).cardToCardDto(newCard);
    }

    @Test
    void createCardWithRetriesTest() {
        long uid = 11L;
        CardCreateDto cardCreateDto = testUtils.getCardCreateDto(uid);
        BaseUser user = testUtils.getBaseUser(uid);
        BaseCard card1 = testUtils.getBaseCard(0L, user, CardStatus.ACTIVE);
        BaseCard card2 = testUtils.getBaseCard(0L, user, CardStatus.ACTIVE);
        BaseCard card3 = testUtils.getBaseCard(123L, user, CardStatus.ACTIVE);

        when(userRepository.findById(uid)).thenReturn(Optional.of(user));
        when(cardFactory.createCard(user)).thenReturn(card1, card2, card3);

        when(cardRepository.saveAndFlush(any(BaseCard.class)))
                .thenThrow(new DataIntegrityViolationException("some"))
                .thenThrow(new DataIntegrityViolationException("some"))
                .thenReturn(card3);

        when(mapper.cardToCardDto(card3)).thenReturn(testUtils.getCardDto());

        CardDto result = service.createCard(cardCreateDto);

        assertThat(result).isNotNull();
        verify(cardRepository, times(3)).saveAndFlush(any(BaseCard.class));
    }

    @Test
    void createCardWithInvalidUserIdTest() {
        CardCreateDto cardCreateDto = testUtils.getCardCreateDto(0L);
        assertThatThrownBy(() -> service.createCard(cardCreateDto))
                .isInstanceOf(UserNotFoundException.class);
        verifyNoInteractions(cardRepository, cardFactory, mapper);
    }

    @Test

    void createCardUserNotFoundTest() {
        CardCreateDto cardCreateDto = testUtils.getCardCreateDto(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createCard(cardCreateDto))
                .isInstanceOf(UserNotFoundException.class);
        verify(cardRepository, never()).saveAndFlush(any());
    }


    @Test
    void getCardsUserTest() {
        CardSearchRequestDto req = new CardSearchRequestDto(
                0, 10, "id,desc",
                null, null, null, null,
                null, null,
                null, null, null
        );

        when(securityUtils.isAdmin()).thenReturn(false);
        when(securityUtils.currentUserId()).thenReturn(777L);

        Page<BaseCard> page = new PageImpl<>(java.util.List.of(testUtils.getBaseCard(1L, testUtils.getBaseUser(777L), CardStatus.ACTIVE)));
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.cardToCardDto(any())).thenReturn(testUtils.getCardDto());

        Page<CardDto> result = service.getCards(req);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getCardsAdminTest() {
        CardSearchRequestDto req = new CardSearchRequestDto(
                1, 5, "createdAt,asc",
                42L, "1234", CardStatus.BLOCKED, LocalDateTime.now().plusYears(5), null,
                new BigDecimal("100.00"), new BigDecimal("500.00"),
                LocalDateTime.now().minusDays(3), LocalDateTime.now());

        when(securityUtils.isAdmin()).thenReturn(true);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        Page<CardDto> result = service.getCards(req);

        assertThat(result).isNotNull();
        verify(securityUtils, never()).currentUserId();
    }

    @Test
    void cardDeleteSuccessTest() {
        BaseCard c = testUtils.getBaseCard(1L, testUtils.getBaseUser(12L), CardStatus.ACTIVE );
        when(cardRepository.findById(1L)).thenReturn(Optional.of(c));

        service.deleteCard(1L);

        verify(cardRepository).delete(c);
    }

    @Test
    void cardDeleteNotFoundTest() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteCard(1L))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void findCardForUpdateSuccessTest() {
        BaseCard c = testUtils.getBaseCard(3L, testUtils.getBaseUser(12L), CardStatus.ACTIVE );
        when(cardRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(c));
        BaseCard r = service.findCardByIdForUpdate(3L);
        assertThat(r).isSameAs(c);
    }

    @Test
    void findByCardNumberSuccessTest() {
        BaseCard c = testUtils.getBaseCard(4L, testUtils.getBaseUser(12L), CardStatus.ACTIVE );
        when(cardRepository.findByNumHmac("HMAC")).thenReturn(Optional.of(c));
        BaseCard result = service.findByCardNumber("HMAC");
        assertThat(result).isSameAs(c);
    }


}
