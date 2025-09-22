package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferResultDto;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.TransferService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionsController.class)
@Import(SecurityConfig.class)
public class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

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
    @WithMockUser(roles = "USER")
    void balanceTransferSuccessTest() throws Exception {
        String bodyJson = """
            {
              "sourceCardId": 101,
              "destinationCardId": 202,
              "amount": 1500.00
            }
            """;

        TransferResultDto resultMock = Mockito.mock(TransferResultDto.class);
        when(transferService.balanceTransfer(any(TransferDto.class))).thenReturn(resultMock);

        mockMvc.perform(post("/api/v1/transaction")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TransactionsController.class))
                .andExpect(handler().methodName("balanceTransfer"));
        verify(transferService).balanceTransfer(any(TransferDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void balanceTransferBadRequestTest() throws Exception {
        mockMvc.perform(post("/api/v1/transaction")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transferService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalanceByIdOkTest() throws Exception {
        when(transferService.getBalance(1001L)).thenReturn(new BigDecimal("12345.67"));

        mockMvc.perform(get("/api/v1/transaction/{cardId}", 1001L)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TransactionsController.class))
                .andExpect(handler().methodName("getBalanceByID"))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().string("12345.67"));
        verify(transferService).getBalance(1001L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalanceByIdValidationFailTest() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/{cardId}", 0L)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transferService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalanceByNumberSuccessTest() throws Exception {
        when(transferService.getBalance("2200700000001234"))
                .thenReturn(new BigDecimal("9876.54"));

        mockMvc.perform(get("/api/v1/transaction/number/{cardNumber}", "2200700000001234")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TransactionsController.class))
                .andExpect(handler().methodName("getBalanceByNumber"))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().string("9876.54"));
        verify(transferService).getBalance("2200700000001234");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalanceByNumberValidationFailTest() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/number/{cardNumber}", "12345")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transferService);
    }

    @Test
    void anyEndpointUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/1001")
                        .accept(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(transferService);
    }

}
