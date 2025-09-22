package com.example.bankcards.repository.spec;

import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.UserRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSpecs {


    public static Specification<BaseUser> nameContains(String name) {
        return (root, q, cb) -> isBlank(name) ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<BaseUser> emailContains(String email) {
        return (root, q, cb) -> isBlank(email) ? null :
                cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<BaseUser> roleEq(UserRole role) {
        return (root, q, cb) -> role == null ? null :
                cb.equal(root.get("role"), role);
    }

    public static Specification<BaseUser> isActiveEq(Boolean active) {
        return (root, q, cb) -> active == null ? null :
                cb.equal(root.get("isActive"), active);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}

