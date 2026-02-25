package com.analog.domain.diary.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analog.domain.diary.dto.request.CreateRequest;
import com.analog.domain.diary.entity.Diary;
import com.analog.domain.diary.repository.DiaryRepository;
import com.analog.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

	private final DiaryRepository diaryRepository;
	
	public void createDiary(User user, CreateRequest request) {
		diaryRepository.save(Diary.create(user, request.title(), request.content(), request.diaryDate()));
	}
}
