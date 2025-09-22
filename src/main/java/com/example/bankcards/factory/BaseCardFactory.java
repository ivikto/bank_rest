package com.example.bankcards.factory;

import com.example.bankcards.entity.*;
import com.example.bankcards.util.NumEncryptor;
import com.example.bankcards.util.NumGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BaseCardFactory implements CardFactory {

    @Value("${card.number.expire-years}")
    private int expireYears;

    private final NumGenerator numGenerator;
    private final NumEncryptor numEncryptor;

    public BaseCard createCard(BaseUser user) {

        String generatedNumber = numGenerator.generateNum();

        return  StandardCard.builder()
                .numEncrypted(numEncryptor.encryptPan(generatedNumber))
                .numHmac(numEncryptor.hmacPan(generatedNumber))
                .cardNumberLast4(generatedNumber.substring(generatedNumber.length() - 4))
                .user(user)
                .expiration(LocalDateTime.now().plusYears(expireYears))
                .cardStatus(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();
    }
}
