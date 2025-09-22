package com.example.bankcards.factory;

import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.User;

public interface CardFactory {

    public BaseCard createCard(BaseUser user);
}
