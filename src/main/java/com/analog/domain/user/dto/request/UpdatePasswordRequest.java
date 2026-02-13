package com.analog.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(
		@NotBlank
		String currentPassword,
		
		@NotBlank
		String newPassword,
		
		@NotBlank
		String newPasswordConfirm
) {

}
