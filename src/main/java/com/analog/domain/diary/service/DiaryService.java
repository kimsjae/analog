package com.analog.domain.diary.service;

import com.analog.domain.diary.dto.request.CreateRequest;
import com.analog.domain.user.entity.User;

public interface DiaryService {

	void createDiary(User user, CreateRequest request);
}
