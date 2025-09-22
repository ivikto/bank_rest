package com.example.bankcards.entity;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.Objects;

@Entity
@DiscriminatorValue("STANDARD")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card extends BaseCard {


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
        Card card = (Card) o;
        return Objects.equals(id, card.id) && Objects.equals(numHmac, card.numHmac) && Objects.equals(standardUser, card.standardUser) && Objects.equals(expiration, card.expiration) && cardStatus == card.cardStatus && Objects.equals(balance, card.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numHmac, standardUser, expiration, cardStatus, balance);
    }
}
