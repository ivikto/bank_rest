package com.example.bankcards.controller;

import com.example.bankcards.TestUtils;
import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserSearchRequestDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.entity.UserType;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

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
    void createUserSuccessTest() throws Exception {
        UserCreateDto createDto = testUtils.getUserCreateDto(
                "Test",
                "Test@mail.com",
                UserRole.USER,
                UserType.STANDARD,
                "pass");

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("createUser"));

        verify(userService).createUser(any(UserCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUserForbiddenTest() throws Exception {
        UserCreateDto createDto = testUtils.getUserCreateDto(
                "Test",
                "Test@mail.com",
                UserRole.USER,
                UserType.STANDARD,
                "pass");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserSuccessTest() throws Exception {
        when(userService.getUser(1001L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/user/{userId}", 1001L)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("getUser"));

        verify(userService).getUser(1001L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserForbiddenTest() throws Exception {
        mockMvc.perform(get("/api/v1/user/{userId}", 1001L)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersSuccessTest() throws Exception {
        UserSearchRequestDto req = testUtils.getUserSearchRequestDto(
                1, 5, "createdAt,desc", "al", "example.com", UserRole.USER, true
        );

        Page<UserDto> page = Page.empty();
        when(userService.getUsers(req)).thenReturn(page);

        mockMvc.perform(get("/api/v1/user")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("getAllUsers"));

        verify(userService).getUsers(req);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersForbiddenTest() throws Exception {
        UserSearchRequestDto req = testUtils.getUserSearchRequestDto(
                0, 10, "createdAt,desc", null, null, null, null
        );

        mockMvc.perform(get("/api/v1/user")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserNoContentTest() throws Exception {
        doNothing().when(userService).deleteUser(777L);

        mockMvc.perform(delete("/api/v1/user/{userId}", 777L))
                .andExpect(status().isNoContent())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("deleteUser"));

        verify(userService).deleteUser(777L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUserForbiddenTest() throws Exception {
        mockMvc.perform(delete("/api/v1/user/{userId}", 777L))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserSuccessTest() throws Exception {
        Long userId = 555L;
        UserUpdateDto updateDto = testUtils.getUserUpdateDto();

        when(userService.updateUser(eq(userId), any(UserUpdateDto.class))).thenReturn(null);

        mockMvc.perform(patch("/api/v1/user/{userId}", userId)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("updateUser"));

        verify(userService).updateUser(eq(userId), any(UserUpdateDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserForbiddenTest() throws Exception {
        Long userId = 555L;
        UserUpdateDto updateDto = testUtils.getUserUpdateDto();

        mockMvc.perform(patch("/api/v1/user/{userId}", userId)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void anyEndpointUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/api/v1/user/1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }
}
