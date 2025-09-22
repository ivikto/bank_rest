package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = {"passwordHash", "version"})
public abstract class BaseUser implements User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @Column(nullable = false)
    public String name;
    @Column(nullable = false)
    public String email;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public List<BaseCard> cards;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public UserRole role;
    @Column(name = "is_active", nullable = false)
    public boolean isActive;
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    public LocalDateTime modifiedAt;
    @Column(name = "version", nullable = false)
    private Long version;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
