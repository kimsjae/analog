package com.analog.domain.auth.refreshToken.entity;

import java.time.Instant;

import com.analog.domain.user.entity.User;
import com.analog.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "refresh_tokens",
		uniqueConstraints = @UniqueConstraint(name = "uk_refresh_tokens_user_id", columnNames = "user_id")
		)
public class RefreshToken extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "kf_refresh_tokens_users"))
	private User user;
	
	@Column(name = "token_hash", nullable = false, length = 64)
	private String tokenHash;
	
	@Column(name = "token_id")
	private String tokenId;
	
	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;
	
	private RefreshToken(User user, String tokenHash, String tokenId, Instant expiresAt) {
		this.user = user;
        this.tokenHash = tokenHash;
        this.tokenId = tokenId;
        this.expiresAt = expiresAt;
	}
	
	public static RefreshToken create(User user, String tokenHash, String tokenId, Instant expiresAt) {
		return new RefreshToken(user, tokenHash, tokenId, expiresAt);
	}
	
	public void rotate(String newTokenHash, String newTokenId, Instant newExpiresAt) {
		this.tokenHash = newTokenHash;
        this.tokenId = newTokenId;
        this.expiresAt = newExpiresAt;
	}
}
