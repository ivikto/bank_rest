package com.example.bankcards.controller;

import com.example.bankcards.TestUtils;
import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardCreateDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequestDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(SecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private final TestUtils testUtils = new TestUtils();

    @BeforeEach
    void makeJwtFilterPassThrough() throws Exception {
        if (jwtAuthFilter != null) {
            doAnswer(inv -> {
                ServletRequest req = inv.getArgument(0);
                ServletResponse res = inv.getArgument(1);
                FilterChain chain = inv.getArgument(2);
                chain.doFilter(req, res);
                return null;
            }).when(jwtAuthFilter).doFilter(any(), any(), any());
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCardOkTest() throws Exception {
        CardCreateDto createDto = new CardCreateDto(42L);
        CardDto dto = testUtils.buildActiveCardDto();

        when(cardService.createCard(createDto)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/card")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)                 // ← добавь это
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))  // теперь не упадёт
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(100.00))
                .andExpect(jsonPath("$.expiration").value("2027-12-31T00:00"));

        verify(cardService).createCard(createDto);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCardForbiddenTest() throws Exception {
        CardCreateDto createDto = new CardCreateDto(42L);

        mockMvc.perform(post("/api/v1/card")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cardService);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCardsSuccessTest() throws Exception {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
        CardSearchRequestDto req = new CardSearchRequestDto(
                0, 10, "id,desc",
                42L, "1234", CardStatus.ACTIVE,
                null, null,
                null, null,
                now.minusDays(7), now
        );

        CardDto cardDto = testUtils.buildActiveCardDto();
        Page<CardDto> page = new PageImpl<>(List.of(cardDto));

        when(cardService.getCards(any(CardSearchRequestDto.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/card")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        ArgumentCaptor<CardSearchRequestDto> captor = ArgumentCaptor.forClass(CardSearchRequestDto.class);
        verify(cardService).getCards(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(42L);
        assertThat(captor.getValue().last4()).isEqualTo("1234");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCardByIdSuccessTest() throws Exception {
        CardDto dto = testUtils.buildActiveCardDto();

        when(cardService.getCard(7L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/card/{cardId}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 1234"));

        verify(cardService).getCard(7L);
    }


    @Test
    @WithMockUser(roles = "USER")
    void getCardByNumberSuccessTest() throws Exception {
        CardDto dto = testUtils.buildActiveCardDto();

        when(cardService.getCard("2200700000000002")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/card/number/{cardNumber}", "2200700000000002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService).getCard("2200700000000002");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCardSuccessTest() throws Exception {
        // Новый порядок аргументов: (cardId, userId, status)
        CardUpdateDto updateDto = new CardUpdateDto(1L, 99L, CardStatus.BLOCKED);
        CardDto dto = testUtils.buildBlockedCardDto();

        when(cardService.updateCard(updateDto)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/card")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.balance").value(200.00));

        verify(cardService).updateCard(updateDto);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCardForbiddenTest() throws Exception {
        CardUpdateDto updateDto = new CardUpdateDto(1L, 99L, CardStatus.BLOCKED);

        mockMvc.perform(patch("/api/v1/card")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cardService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCardNoContentTest() throws Exception {
        doNothing().when(cardService).deleteCard(123L);

        mockMvc.perform(delete("/api/v1/card/{cardId}", 123L))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(123L);
    }

    @Test
    void anyEndpointUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/api/v1/card/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cardService);
    }

}
