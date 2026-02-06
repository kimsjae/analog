package com.analog.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "email을 입력해주세요.")
		String email,
		
		@NotBlank(message = "password를 입력해주세요.")
		String password
) {

}
