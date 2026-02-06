package com.analog.domain.auth.dto.response;

public record LoginResponse(
		Long userId,
		String email,
		String name,
		String accessToken
) {

}
