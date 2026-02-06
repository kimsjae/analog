package com.analog.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
		String issuer,
		String secret,
		long accessTokenExpSeconds,
		long refreshTokenExpSeconds
) {

}
