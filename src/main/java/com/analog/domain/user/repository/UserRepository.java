package com.analog.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.analog.domain.user.entity.AuthProvider;
import com.analog.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
	
	Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
	
	boolean existsByEmail(String email);
	
	boolean existsByProviderAndProviderId(AuthProvider provider, String providerId);
}
