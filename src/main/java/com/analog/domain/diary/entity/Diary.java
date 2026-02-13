package com.analog.domain.diary.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.analog.domain.user.entity.User;
import com.analog.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diaries",
        indexes = {
                @Index(name = "idx_diaries_user_id", columnList = "user_id"),
                @Index(name = "idx_diaries_diary_date", columnList = "diary_date"),
                @Index(name = "idx_diaries_deleted_at", columnList = "deleted_at")
        }
)
public class Diary extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	private String title;
	
	@Lob
    @Column(nullable = false)
	private String content;
	
	@Column(name = "diary_date", nullable = false)
	private LocalDate diaryDate;
	
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
	private Diary(User user, String title, String content, LocalDate diaryDate) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.diaryDate = diaryDate;
    }
	
	public static Diary create(User user, String title, String content, LocalDate diaryDate) {
        return new Diary(user, title, content, diaryDate);
    }
}
