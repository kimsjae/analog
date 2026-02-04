package com.analog.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.analog.domain.user.entity.AuthProvider;
import com.analog.domain.user.entity.User;

@DataJpaTest
class UserRepositoryTest {

	@TestConfiguration
	@EnableJpaAuditing
	static class JpaAuditingTestConfig {}
	
	@Autowired
	private UserRepository userRepository;
	
	@Test
	void localUser_save_and_findByEmail() {
		// given
		User user = User.createLocal(
				"test@test.com",
				"hashed-password",
				"tester"
		);
		
		// when
		userRepository.saveAndFlush(user);
		
		// then
		User found = userRepository.findByEmail("test@test.com").orElseThrow();
		assertThat(found.getEmail()).isEqualTo("test@test.com");
		assertThat(found.isLocal()).isTrue();
		assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
	}
	
	@Test
    void oauthUser_unique_provider_and_providerId() {
        // given
        User user1 = User.createOAuth(
            AuthProvider.GOOGLE,
            "google-123",
            "user@test.com",
            "user1"
        );
        User user2 = User.createOAuth(
            AuthProvider.GOOGLE,
            "google-123",
            "other@test.com",
            "user2"
        );

        // when
        userRepository.saveAndFlush(user1);

        // then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
            .isInstanceOf(Exception.class);
    }
}
