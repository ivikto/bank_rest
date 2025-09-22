package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;

public interface CardFactory {

    public Card createCard(User user, int yearsToExpire);
}
