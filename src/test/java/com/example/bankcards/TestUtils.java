package com.example.bankcards;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;

public class TestUtils {

    public UserCreateDto getUserCreateDto(String name, String email, UserRole role, UserType type, String password) {

        return new UserCreateDto(name, email, role, type, password);
    }

    public BaseUser getBaseUser(UserCreateDto dto) {

        return StandardUser.builder()
                .id(1L)
                .name(dto.name())
                .email(dto.email())
                .cards(null)
                .role(dto.role())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public BaseUser getBaseUser() {

        return new StandardUser();
    }

    public BaseUser getBaseUser(Long id) {

        return StandardUser.builder()
                .id(id)
                .build();
    }

    public BaseUser getBaseUser(Long userId, UserUpdateDto dto) {

        return StandardUser.builder()
                .id(userId)
                .name(dto.name())
                .email(dto.email())
                .role(dto.role())
                .build();
    }

    public BaseUser getBaseUser(Long id, String name, String email, UserRole role, boolean isActive) {

        return StandardUser.builder()
                .id(id)
                .name(name)
                .email(email)
                .role(role)
                .isActive(isActive)
                .build();
    }

    public UserDto getUserDto(BaseUser user) {

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .cards(List.of())
                .role(user.getRole())
                .isActive(user.isActive)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserUpdateDto getUserUpdateDto() {

        return new UserUpdateDto("Test", "test@test", UserRole.USER);
    }

    public TransferDto getTransferDto(Long sourceId, Long destId, BigDecimal amount) {

        return new TransferDto(sourceId, destId, amount);
    }

    public BaseCard getBaseCard(Long cardId) {
        return StandardCard.builder()
                .id(cardId)
                .user(getBaseUser())
                .balance(new BigDecimal("1000"))
                .cardStatus(CardStatus.ACTIVE)
                .cardNumberLast4("9999")
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .expiration(LocalDateTime.now().plusYears(5))
                .build();
    }

    public BaseCard getBaseCard(long id, BaseUser owner, CardStatus status) {
        return StandardCard.builder()
                .id(id)
                .user(owner)
                .cardStatus(status)

                .build();
    }

    public List<BaseCard> getListOfCards() {

        return List.of(getBaseCard(9L, getBaseUser(1L), CardStatus.ACTIVE),
                getBaseCard(10L, getBaseUser(2L), CardStatus.ACTIVE));
    }

    public CardDto getCardDto() {
        return mock(CardDto.class);
    }

    public CardDto buildActiveCardDto() {
        return new CardDto(
                1L,
                "**** **** **** 1234",
                "ACTIVE",
                new BigDecimal("100.00"),
                "2027-12-31T00:00"
        );
    }

    public CardDto buildBlockedCardDto() {
        return new CardDto(
                1L,
                "**** **** **** 1234",
                "BLOCKED",
                new BigDecimal("200.00"),
                "2027-12-31T00:00"
        );
    }

    public UserSearchRequestDto getUserSearchRequestDto(int page, int size,
                                                        String sort, String name,
                                                        String email, UserRole role,
                                                        Boolean isActive) {

        return new UserSearchRequestDto(page, size, sort, name, email, role, isActive);
        }

        public CardCreateDto getCardCreateDto(Long id) {

        return new CardCreateDto(id);
        }

    }
