package com.example.bankcards.factory;

import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.User;

public interface UserFactory {

    public BaseUser createUser(UserCreateDto userCreateDto);
}
