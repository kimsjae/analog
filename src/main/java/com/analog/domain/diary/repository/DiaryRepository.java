package com.analog.domain.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analog.domain.diary.entity.Diary;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

	long deleteByUserId(Long userId);
}
