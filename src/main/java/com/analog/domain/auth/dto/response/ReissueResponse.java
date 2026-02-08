package com.analog.domain.auth.dto.response;

public record ReissueResponse(
		String accessToken,
		String refreshToken
) {

}
