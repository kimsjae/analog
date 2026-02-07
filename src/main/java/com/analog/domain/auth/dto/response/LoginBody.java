package com.analog.domain.auth.dto.response;

public record LoginBody(
		Long userId,
		String email,
		String name,
		String accessToken
) {

}
