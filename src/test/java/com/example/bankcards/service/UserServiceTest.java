package com.example.bankcards.service;

import com.example.bankcards.TestUtils;
import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.dto.UserUpdateDto;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.entity.UserType;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.factory.UserFactory;
import com.example.bankcards.mapper.BankMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFactory userFactory;
    @Mock
    private BankMapper mapper;

    private final TestUtils testUtils = new TestUtils();


    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void createUserNullTest() {

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(null));

        verifyNoInteractions(userRepository, userFactory, mapper);
    }

    @Test
    void createUserBlackTest() {
        UserCreateDto dto = testUtils.getUserCreateDto("", "", UserRole.USER, UserType.STANDARD, "");

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(dto));

        verifyNoInteractions(userRepository, userFactory, mapper);
    }

    @Test
    void createUserSuccessTest() {
        UserCreateDto dto = testUtils.getUserCreateDto("Test", "Test@mail.ru", UserRole.USER, UserType.STANDARD, "");
        BaseUser user = testUtils.getBaseUser(dto);
        UserDto resultUserDto = testUtils.getUserDto(user);


        when(userFactory.createUser(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(mapper.userToUserDTO(user)).thenReturn(resultUserDto);

        UserDto result = userService.createUser(dto);

        assertEquals(user.id, result.id);
        assertEquals(user.name, result.name);
        assertEquals(user.email, result.email);
        assertEquals(user.role, result.role);

        InOrder inOrder = inOrder(userFactory, userRepository, mapper);
        inOrder.verify(userFactory).createUser(dto);
        inOrder.verify(userRepository).save(user);
        inOrder.verify(mapper).userToUserDTO(user);
        verifyNoMoreInteractions(userFactory, userRepository, mapper);
    }

    @Test
    void getUserNullTest() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUser(null));

        verifyNoInteractions(userRepository, mapper);
    }

    @Test
    void getUserSuccessTest() {
        Long userId = 1L;

        BaseUser user = testUtils.getBaseUser();
        user.setId(userId);
        UserDto userDto = testUtils.getUserDto(user);

        when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.of(user));
        when(mapper.userToUserDTO(user)).thenReturn(userDto);

        UserDto result = userService.getUser(userId);

        assertEquals(userId, result.id);
    }

    @Test
    void getUserUnSuccessTest() {
        Long userId = 9999L;

        when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUser(userId));

        verifyNoInteractions(mapper);
    }

    @Test
    void getUsersNegativePageTest() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUsers(testUtils.getUserSearchRequestDto(-500, 10, null, null, null, null, null)));
        verifyNoInteractions(userRepository, mapper);
    }

    @Test
    void getUsersZeroSizeTest() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUsers(testUtils.getUserSearchRequestDto(0, 0, null, null, null, null, null)));
        verifyNoInteractions(userRepository, mapper);
    }

    @Test
    void getUsersSuccessTest() {
        int page = 0, size = 5;
        String sort = "id,asc";

        BaseUser user1 = testUtils.getBaseUser(1L, "Alice", "a@ex.com", UserRole.USER, true);
        BaseUser user2 = testUtils.getBaseUser(2L, "Bob",   "b@ex.com", UserRole.ADMIN, false);
        Page<BaseUser> repoPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(page, size), 2);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)))
                .thenReturn(repoPage);

        UserDto dto1 = testUtils.getUserDto(user1);
        UserDto dto2 = testUtils.getUserDto(user2);
        when(mapper.userToUserDTO(user1)).thenReturn(dto1);
        when(mapper.userToUserDTO(user2)).thenReturn(dto2);

        Page<UserDto> result = userService.getUsers(testUtils.getUserSearchRequestDto(page, size, sort,
                "Al", "ex.com", UserRole.USER, true));

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(dto1, dto2), result.getContent());

        verify(mapper).userToUserDTO(user1);
        verify(mapper).userToUserDTO(user2);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void deleteUserNullTest() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(null));

        verifyNoInteractions(userRepository);
    }

    @Test
    void deleteUserNotFoundTest() {
        Long userId = 9999999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(userId));

    }

    @Test
    void deleteUserSuccessTest() {

        Long userId = 2L;
        BaseUser user = testUtils.getBaseUser(userId, "Bob",   "b@ex.com", UserRole.ADMIN, false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.deleteUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void updateUserNullTest() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, null));

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUserNotFoundTest() {
        Long userId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(userId, testUtils.getUserUpdateDto()));
    }

    @Test
    void updateUserSuccessFoundTest() {
        Long userId = 2L;
        UserUpdateDto userUpdateDto = testUtils.getUserUpdateDto();
        BaseUser user = testUtils.getBaseUser();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        user.setName(userUpdateDto.name());
        user.setEmail(userUpdateDto.email());
        user.setRole(userUpdateDto.role());

        when(userRepository.save(user)).thenReturn(user);

        UserDto userDto = testUtils.getUserDto(user);

        when(mapper.userToUserDTO(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(userId, userUpdateDto);

        assertEquals(userUpdateDto.name(), result.name);

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verify(mapper).userToUserDTO(user);
    }










}
