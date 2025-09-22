package com.example.bankcards.factory;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.entity.StandardUser;
import com.example.bankcards.entity.User;

import java.time.LocalDateTime;

import static com.example.bankcards.entity.UserType.STANDARD;

public class StandardUserFactory implements UserFactory {

    @Override
    public User createUser(UserCreateDto userCreateDto) {

        return switch (userCreateDto.userType()) {
            case STANDARD -> StandardUser.builder()
                    .name(userCreateDto.name())
                    .email(userCreateDto.email())
                    .role(userCreateDto.role())
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .isActive(true)
                    .build();
            case PREMIUM ->
        };
    }
}
