package com.example.bankcards.repository;

import com.example.bankcards.entity.BaseCard;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CardRepository extends JpaRepository<BaseCard, Long>, JpaSpecificationExecutor<BaseCard> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BaseCard c where c.id = :id")
    @QueryHints(@QueryHint(name="jakarta.persistence.lock.timeout", value="3000"))
    Optional<BaseCard> findByIdForUpdate(@Param("id") Long id);

    Optional<BaseCard> findByNumHmac(String numHmac);
}
