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
public class StandardCard extends BaseCard {


    @Override
    public String toString() {
        return "Card{" +
                "id=" + this.id +
                ", CardNumber='" + getCardNumber() + '\'' +
                ", expiration=" + expiration +
                ", status=" + cardStatus +
                ", balance=" + balance +
                '}';
    }

    public String getCardNumber() {
        return "**** **** **** " + this.cardNumberLast4;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StandardCard card = (StandardCard) o;
        return Objects.equals(id, card.id) && Objects.equals(numHmac, card.numHmac) && Objects.equals(user, card.user) && Objects.equals(expiration, card.expiration) && cardStatus == card.cardStatus && Objects.equals(balance, card.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numHmac, user, expiration, cardStatus, balance);
    }

}
