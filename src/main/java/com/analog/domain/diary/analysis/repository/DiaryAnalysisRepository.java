package com.analog.domain.diary.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analog.domain.diary.analysis.entity.DiaryAnalysis;

public interface DiaryAnalysisRepository extends JpaRepository<DiaryAnalysis, Long> {

	long deleteByDiaryUserId(Long userId);
}
