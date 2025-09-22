package com.example.bankcards.util;

import com.example.bankcards.dto.CardSearchRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class CardsSearchFilterPolicy {

    public void checkFilter(CardSearchRequestDto dto) {

        int page = (dto.page() == null || dto.page() < 0) ? 0 : dto.page();
        int size = (dto.size() == null || dto.size() < 1) ? 20 : Math.min(dto.size(), 100);
        String last4 = dto.last4();
        var expirationFrom = dto.expirationFrom();
        var expirationTo = dto.expirationTo();
        BigDecimal balanceMin = dto.balanceMin();
        BigDecimal balanceMax = dto.balanceMax();
        var createdFrom = dto.createdFrom();
        var createdTo = dto.createdTo();

        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be in [1,100]");

        if (last4 != null && !last4.matches("\\d{4}")) {
            log.warn("Validation failed: last4 must be 4 digits");
            throw new IllegalArgumentException("last4 must be 4 digits");
        }
        if (balanceMin != null && balanceMax != null && balanceMin.compareTo(balanceMax) > 0) {
            log.warn("Validation failed: balanceMin must be <= balanceMax");
            throw new IllegalArgumentException("balanceMin must be <= balanceMax");
        }

        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            log.warn("Validation failed: createdFrom must be <= createdTo");
            throw new IllegalArgumentException("createdFrom must be <= createdTo");
        }

        if (expirationFrom != null && expirationTo != null && expirationFrom.isAfter(expirationTo)) {
            log.warn("Validation failed: expirationFrom must be <= expirationTo");
            throw new IllegalArgumentException("expirationFrom must be <= expirationTo");
        }
    }
}
