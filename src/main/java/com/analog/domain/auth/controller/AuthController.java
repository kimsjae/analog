package com.analog.domain.auth.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analog.domain.auth.dto.request.LoginRequest;
import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.AuthTokens;
import com.analog.domain.auth.dto.response.LoginResponse;
import com.analog.domain.auth.dto.response.SignupResponse;
import com.analog.domain.auth.service.AuthService;
import com.analog.global.config.RefreshCookieProperties;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final RefreshCookieProperties refreshCookieProperties;

	
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@RequestBody @Valid SignupRequest request) {
		SignupResponse response = authService.signup(request);
		
		return ResponseEntity
				.created(URI.create("/api/users/" + response.userId()))
				.body(response);
	}
	
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
		AuthTokens tokens = authService.login(request);
		
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
				.httpOnly(true)
				.secure(refreshCookieProperties.secure())
				.sameSite("Lax")
				.path("/api/auth")
				.build();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(tokens.response());
	}
}
