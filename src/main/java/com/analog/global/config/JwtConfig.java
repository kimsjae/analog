package com.analog.global.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.analog.global.security.jwt.JwtTokenProvider;

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
