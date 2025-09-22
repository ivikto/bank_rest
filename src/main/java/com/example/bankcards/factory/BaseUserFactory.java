package com.example.bankcards.factory;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.StandardUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BaseUserFactory implements UserFactory {

    private final PasswordEncoder encoder;

    @Override
    public BaseUser createUser(UserCreateDto userCreateDto) {

        return switch (userCreateDto.userType()) {
            case STANDARD -> StandardUser.builder()
                    .name(userCreateDto.name())
                    .email(userCreateDto.email())
                    .role(userCreateDto.role())
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .isActive(true)
                    .passwordHash(encoder.encode(userCreateDto.password()))
                    .version(0L)
                    .build();
        };
    }
}
