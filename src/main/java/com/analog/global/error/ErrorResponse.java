package com.analog.global.error;

import java.time.LocalDateTime;

public record ErrorResponse(
		String errorCode,
		String message,
		LocalDateTime timestamp,
		String path
		) {

	public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
		return new ErrorResponse(errorCode.name(), message, LocalDateTime.now(), path);
	}
	
	public static ErrorResponse of(ErrorCode errorCode, String path) {
		return new ErrorResponse(errorCode.name(), errorCode.getDefaultMessage(), LocalDateTime.now(), path);
	}
}
