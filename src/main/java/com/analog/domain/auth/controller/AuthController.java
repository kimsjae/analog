package com.analog.domain.auth.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analog.domain.auth.dto.request.LoginRequest;
import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.LoginBody;
import com.analog.domain.auth.dto.response.LoginResponse;
import com.analog.domain.auth.dto.response.ReissueResponse;
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
	public ResponseEntity<LoginBody> login(@RequestBody @Valid LoginRequest request) {
		LoginResponse response = authService.login(request);
		
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
				.httpOnly(true)
				.secure(refreshCookieProperties.secure())
				.sameSite("Lax")
				.path("/api/auth")
				.build();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(response.body());
	}
	
	@PostMapping("/reissue")
	public ResponseEntity<String> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
		ReissueResponse response = authService.reissue(refreshToken);
		
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
				.httpOnly(true)
				.secure(refreshCookieProperties.secure())
				.sameSite("Lax")
				.path("/api/auth")
				.build();
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(response.accessToken());
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
		authService.logout(refreshToken);
		
		ResponseCookie expired = ResponseCookie.from("refreshToken")
				.httpOnly(true)
				.secure(refreshCookieProperties.secure())
				.sameSite("Lax")
				.path("/api/auth")
				.maxAge(0)
				.build();
		
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, expired.toString())
				.build();
	}
}
