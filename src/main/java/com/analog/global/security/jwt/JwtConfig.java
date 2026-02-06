package com.analog.global.security.jwt;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
	
	@Bean
	public JwtTokenProvider jwtTokenProvider(JwtProperties props, Clock clock) {
		return new JwtTokenProvider(props, clock);
	}
}
