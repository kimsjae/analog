package com.analog.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// 비즈니스 예외
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(
			BusinessException ex,
			HttpServletRequest request
	) {
		ErrorCode errorCode = ex.getErrorCode();
		
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ErrorResponse.of(
						errorCode,
						ex.getMessage(),
						request.getRequestURI()
				));
	}

	// @Valid 검증 실패
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse(ErrorCode.REQ_400.getDefaultMessage());
		
		return ResponseEntity
				.status(ErrorCode.REQ_400.getHttpStatus())
				.body(ErrorResponse.of(
						ErrorCode.REQ_400,
						message,
						request.getRequestURI()
						));
	}
	
	// JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ErrorCode.REQ_400.getHttpStatus())
                .body(ErrorResponse.of(
                        ErrorCode.REQ_400,
                        request.getRequestURI()
                ));
    }
    
    // 예상 못 한 서버 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ErrorCode.SRV_500.getHttpStatus())
                .body(ErrorResponse.of(
                        ErrorCode.SRV_500,
                        request.getRequestURI()
                ));
    }
}
