package com.example.bankcards.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface Card {

    Long getId();

    String getCardNumberLast4();

    BigDecimal getBalance();

    BaseUser getUser();

    CardStatus getCardStatus();

    LocalDateTime getExpiration();

    void setCardStatus(CardStatus status);

    void setUser(BaseUser user);

    void setModifiedAt(LocalDateTime ldt);
}

