package com.park.restapi.domain.auth.repository;

import com.park.restapi.domain.auth.entity.RefreshToken;
import com.park.restapi.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByMember(Member member);

    Optional<RefreshToken> findByValueAndExpireDateGreaterThan(String value, LocalDateTime expireDate);
}
