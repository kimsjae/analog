package com.analog.global.security.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.analog.global.error.ErrorCode;
import com.analog.global.error.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;


@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;
	
	public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
	
	@Override
	public void commence(HttpServletRequest request,
						HttpServletResponse response,
						AuthenticationException authException) throws IOException {
		ErrorCode errorCode = ErrorCode.AUTH_401;
		
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		
		ErrorResponse body = ErrorResponse.of(errorCode, request.getRequestURI());
		
		objectMapper.writeValue(response.getWriter(), body);
	}
}
