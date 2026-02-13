package com.analog.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateMeRequest(
		@NotBlank(message = "변경할 이름을 입력하세요.")
		String name
) {

}
