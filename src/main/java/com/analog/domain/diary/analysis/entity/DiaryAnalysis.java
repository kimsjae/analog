package com.analog.domain.diary.analysis.entity;

import java.time.LocalDateTime;

import com.analog.domain.diary.entity.Diary;
import com.analog.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diary_analyses")
public class DiaryAnalysis extends BaseTimeEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diary_id", nullable = false, unique = true)
    private Diary diary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
    
    private DiaryAnalysis(Diary diary, AnalysisStatus status, LocalDateTime analyzedAt) {
        this.diary = diary;
        this.status = status;
        this.analyzedAt = analyzedAt;
    }

    public static DiaryAnalysis create(Diary diary, AnalysisStatus status, LocalDateTime analyzedAt) {
        return new DiaryAnalysis(diary, status, analyzedAt);
    }
}
