package com.analog.domain.user.dto.response;

public record UpdatePasswordResponse(
		String accessToken,
		String refreshToken
) {

}
