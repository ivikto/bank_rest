package com.example.bankcards.repository;

import com.example.bankcards.entity.BaseCard;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CardRepository extends JpaRepository<BaseCard, Long>, JpaSpecificationExecutor<BaseCard> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BaseCard c where c.id = :id")
    Optional<BaseCard> findByIdForUpdate(@Param("id") Long id);

    Optional<Object> findByNumHmac(String numHmac);
}
