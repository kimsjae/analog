package com.analog.domain.diary.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRequest(
		@NotBlank
		String title,
		
		@NotBlank
		String content,
		
		@NotNull
		LocalDate diaryDate
) {

}
