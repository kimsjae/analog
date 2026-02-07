package com.analog.domain.auth.dto.response;

public record LoginResponse(
		LoginBody body,
		String refreshToken
) {

}
