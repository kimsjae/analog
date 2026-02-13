package com.analog.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(
		@NotBlank
		String password
) {

}
