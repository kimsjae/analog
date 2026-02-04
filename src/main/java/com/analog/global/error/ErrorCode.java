package com.analog.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	REQ_400(HttpStatus.BAD_REQUEST, "Bad Request"),
    AUTH_401(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    AUTH_403(HttpStatus.FORBIDDEN, "Forbidden"),
    RES_404(HttpStatus.NOT_FOUND, "Not Found"),
    RES_409(HttpStatus.CONFLICT, "Conflict"),
    SRV_500(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
	
	private final HttpStatus httpStatus;
	private final String defaultMessage;
	
	ErrorCode(HttpStatus httpStatus, String defaultMessage) {
		this.httpStatus = httpStatus;
		this.defaultMessage = defaultMessage;
	}
	
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	
	public String getDefaultMessage() {
		return defaultMessage;
	}
}
