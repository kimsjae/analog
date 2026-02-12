package com.analog.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analog.domain.user.dto.response.MeResponse;
import com.analog.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	
	@GetMapping("/me")
	public ResponseEntity<MeResponse> me() {
		return ResponseEntity.ok(userService.me());
	}
}
