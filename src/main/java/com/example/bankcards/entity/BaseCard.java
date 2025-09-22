package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "cards")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = {"user", "numEncrypted", "numHmac"})
public abstract class BaseCard implements Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "num_encrypted", nullable = false, length = 1024)
    public String numEncrypted;
    @Column(name = "num_last4", nullable = false, length = 4)
    public String cardNumberLast4;
    @Column(name = "num_hmac", nullable = false, length = 128, unique = true)
    public String numHmac;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public BaseUser user;
    @Column(nullable = false)
    public LocalDateTime expiration;
    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false)
    public CardStatus cardStatus;
    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal balance;
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    public LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    public LocalDateTime modifiedAt;
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
