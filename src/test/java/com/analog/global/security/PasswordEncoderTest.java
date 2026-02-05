package com.analog.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordEncoderTest {

	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Test
	void password_is_hashed_and_matches() {
		// given
		String password = "123123";
		
		// when
		String encoded = passwordEncoder.encode(password);
		
		// then
		assertThat(encoded).isNotEqualTo(password);
		assertThat(passwordEncoder.matches(password, encoded)).isTrue();
	}
}
