package com.example.bankcards.repository;

import com.example.bankcards.entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<BaseUser, Long>, JpaSpecificationExecutor<BaseUser> {
    Optional<BaseUser> findByEmail(String email);

    @Query("""
            select distinct u
            from BaseUser u
            left join fetch u.cards
            where u.id = :id
            """)
    Optional<BaseUser> findByIdWithCards(@Param("id") Long id);
}
