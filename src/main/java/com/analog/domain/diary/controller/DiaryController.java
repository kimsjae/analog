package com.analog.domain.diary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analog.domain.diary.dto.request.CreateRequest;
import com.analog.domain.diary.service.DiaryService;
import com.analog.domain.user.entity.User;
import com.analog.global.security.auth.AuthUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {

	private final DiaryService diaryService;
	
	@PostMapping
	public ResponseEntity<Void> createDiary(@RequestBody @Valid CreateRequest request) {
		User user = AuthUser.requireUser();
		diaryService.createDiary(user, request);
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
