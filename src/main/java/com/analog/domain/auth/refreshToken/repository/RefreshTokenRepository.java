package com.analog.domain.auth.refreshToken.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analog.domain.auth.refreshToken.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByUserId(Long userId);
	
	void deleteByUserId(Long userId);
}
