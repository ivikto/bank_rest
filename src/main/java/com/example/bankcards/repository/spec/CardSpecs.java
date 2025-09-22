package com.example.bankcards.repository.spec;

import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.entity.CardStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardSpecs {

    public static Specification<BaseCard> userIdEq(Long userId) {
        return (root, q, cb) -> userId == null ? null :
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<BaseCard> last4Eq(String last4) {
        return (root, q, cb) -> (last4 == null || last4.isBlank()) ? null :
                cb.equal(root.get("cardNumberLast4"), last4);
    }

    public static Specification<BaseCard> statusEq(CardStatus status) {
        return (root, q, cb) -> status == null ? null :
                cb.equal(root.get("cardStatus"), status);
    }

    public static Specification<BaseCard> expirationFrom(LocalDateTime from) {
        return (root, q, cb) -> from == null ? null :
                cb.greaterThanOrEqualTo(root.get("expiration"), from);
    }

    public static Specification<BaseCard> expirationTo(LocalDateTime to) {
        return (root, q, cb) -> to == null ? null :
                cb.lessThan(root.get("expiration"), to); // можно заменить на <= по вкусу
    }

    public static Specification<BaseCard> balanceGte(BigDecimal min) {
        return (root, q, cb) -> min == null ? null :
                cb.greaterThanOrEqualTo(root.get("balance"), min);
    }

    public static Specification<BaseCard> balanceLte(BigDecimal max) {
        return (root, q, cb) -> max == null ? null :
                cb.lessThanOrEqualTo(root.get("balance"), max);
    }

    public static Specification<BaseCard> createdFrom(LocalDateTime from) {
        return (root, q, cb) -> from == null ? null :
                cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<BaseCard> createdTo(LocalDateTime to) {
        return (root, q, cb) -> to == null ? null :
                cb.lessThan(root.get("createdAt"), to);
    }

}
