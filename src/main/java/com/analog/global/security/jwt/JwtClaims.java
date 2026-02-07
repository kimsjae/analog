package com.analog.global.security.jwt;

import java.time.Instant;

public record JwtClaims(
		Long userId,
		TokenType tokenType,
		String tokenId,
		Instant expiresAt
) {

}
