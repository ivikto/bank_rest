package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@DiscriminatorValue("STANDARD")
@Data
public class User extends BaseUser {


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(name, user.name) && Objects.equals(cards, user.cards) && role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cards, role);
    }
}
