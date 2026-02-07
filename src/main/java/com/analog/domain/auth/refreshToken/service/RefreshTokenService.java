package com.analog.domain.auth.refreshToken.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analog.domain.auth.refreshToken.entity.RefreshToken;
import com.analog.domain.auth.refreshToken.hash.RefreshTokenHasher;
import com.analog.domain.auth.refreshToken.repository.RefreshTokenRepository;
import com.analog.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;
    
    @Transactional
    public void upsert(
    		User user,
    		String rawRefreshToken,
    		String tokenId,
    		Instant expiresAt
    ) {
    	String hash = refreshTokenHasher.hash(rawRefreshToken);
    	
    	refreshTokenRepository.findByUserId(user.getId())
    	.ifPresentOrElse(
    			existing -> existing.rotate(hash, tokenId, expiresAt),
    			() -> refreshTokenRepository.save(
    					RefreshToken.create(user, hash, tokenId, expiresAt)
    					)
    			);
    }
}
