package com.analog.domain.auth.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.SignupResponse;
import com.analog.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@RequestBody @Valid SignupRequest request) {
		SignupResponse response = authService.signup(request);
		
		return ResponseEntity
				.created(URI.create("/api/users/" + response.userId()))
				.body(response);
	}
}
