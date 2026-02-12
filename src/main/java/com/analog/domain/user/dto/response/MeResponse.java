package com.analog.domain.user.dto.response;

public record MeResponse(
		Long userId,
		String email,
		String name
) {

}
