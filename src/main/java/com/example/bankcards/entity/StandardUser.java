package com.example.bankcards.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@DiscriminatorValue("STANDARD")
@Getter @Setter
@SuperBuilder
@NoArgsConstructor
public class StandardUser extends BaseUser {


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StandardUser standardUser = (StandardUser) o;
        return id == standardUser.id && Objects.equals(name, standardUser.name) && Objects.equals(cards, standardUser.cards) && role == standardUser.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cards, role);
    }
}
