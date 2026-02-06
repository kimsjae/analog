package com.analog.domain.auth.dto.response;

public record AuthTokens(
		LoginResponse response,
		String refreshToken
) {

}
