package com.analog.domain.user.entity;

import java.util.Objects;

import com.analog.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "users",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_users_email", columnNames = "email"),
				@UniqueConstraint(name = "uk_users_provider_id", columnNames = {"provider", "provider_id"})
		}
)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String email;
	
	private String password;
	
	private String name;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuthProvider provider;
	
	@Column(name = "provider_id")
	private String providerId;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;

	private User(String email, String password, String name, AuthProvider provider, String providerId, UserRole role) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.provider = Objects.requireNonNull(provider);
		this.providerId = providerId;
		this.role = Objects.requireNonNull(role);
	}
	
	public static User createLocal(String email, String passwordHash, String name) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("email is required for LOCAL");
		}
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new IllegalArgumentException("password is required for LOCAL");
		}
		
		return new User(email.trim(), passwordHash, name, AuthProvider.LOCAL, null, UserRole.USER);
	}
	
	public static User createOAuth(AuthProvider provider, String providerId, String email, String name) {
        if (provider == null || provider == AuthProvider.LOCAL) {
        	throw new IllegalArgumentException("provider must be GOOGLE or KAKAO");
        }
        if (providerId == null || providerId.isBlank()) {
        	throw new IllegalArgumentException("providerId is required for OAuth");
        }

        String normalizedEmail = (email == null || email.isBlank()) ? null : email.trim();
        
        return new User(normalizedEmail, null, name, provider, providerId.trim(), UserRole.USER);
    }
	
	public boolean isLocal() {
		return provider == AuthProvider.LOCAL;
	}
	
	public void changeName(String name) {
		this.name = name;
	}

    public void changePasswordHash(String passwordHash) {
    	this.password = passwordHash;
    }

    public void promoteToAdmin() {
    	this.role = UserRole.ADMIN;
    }
}
