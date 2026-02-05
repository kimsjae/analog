package com.analog.domain.auth.dto.response;

public record SignupResponse(
		Long userId,
		String email,
		String name
) {

}
