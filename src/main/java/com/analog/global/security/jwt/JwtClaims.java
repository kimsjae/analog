package com.analog.global.security.jwt;

public record JwtClaims(
		Long userId,
		String email,
		String name,
		TokenType tokenType
) {

}
