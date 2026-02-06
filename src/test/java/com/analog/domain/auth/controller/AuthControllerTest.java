package com.analog.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}
	
	@Test
	void signup_success_201() throws Exception {
		String body = """
				{
				"email": "test@test.com",
				"password": "123123",
				"name": "tester"
				}
				""";
		
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.userId").isNumber())
		.andExpect(jsonPath("$.email").value("test@test.com"))
		.andExpect(jsonPath("$.name").value("tester"));
		
		User user = userRepository.findAll().get(0);
		assertThat(passwordEncoder.matches("123123", user.getPassword())).isTrue();
	}
	
	@Test
	void signup_duplicateEmail_409() throws Exception {
		userRepository.save(User.createLocal(
				"test@test.com",
				passwordEncoder.encode("456456"),
				"existing"
		));
		
		
		String body = """
				{
				"email": "test@test.com",
                "password": "123123",
                "name": "tester"
				}
				""";
		
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
		.andExpect(status().isConflict())
		.andExpect(jsonPath("$.errorCode").value("RES_409"))
        .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
        .andExpect(jsonPath("$.path").value("/api/auth/signup"));
	}
	
	@Test
	void login_success_200() throws Exception {
		userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
		
		String body = """
				{
				"email": "test@test.com",
				"password": "123123"
				}
				""";
		
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.userId").isNumber())
        .andExpect(jsonPath("$.email").value("test@test.com"));
	}
	
	
	@Test
	void login_fail_400() throws Exception {
		userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
		
		String body = """
				{
				"email": "test11@test.com"
				}
				""";
		
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
		.andExpect(status().isBadRequest());
	}
	
	@Test
	void login_fail_401() throws Exception {
		userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
		
		String body = """
				{
				"email": "test11@test.com",
				"password": "123213"
				}
				""";
		
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
		.andExpect(status().isUnauthorized());
	}
}
