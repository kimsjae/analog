package com.analog.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.analog.domain.auth.refreshToken.entity.RefreshToken;
import com.analog.domain.auth.refreshToken.hash.RefreshTokenHasher;
import com.analog.domain.auth.refreshToken.repository.RefreshTokenRepository;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
	@Autowired
	RefreshTokenHasher refreshTokenHasher;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@AfterEach
	void tearDown() {
		refreshTokenRepository.deleteAll();
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
	
	@Test
	void jwt_login_and_refresh_cookie_success() throws Exception {
		User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
		
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						"email": "test@test.com",
						"password": "123123"
						}
						"""))
		.andExpect(status().isOk())
		.andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")))
		.andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.userId").value(user.getId()))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.name").value("tester"))
        .andExpect(content().string(not(containsString("refreshToken"))));
	}
	
	@Test
	void login_persists_refresh_token_hash() throws Exception {
		User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
		
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						"email": "test@test.com",
						"password": "123123"
						}
						"""))
				.andExpect(status().isOk())
				.andReturn();
		
		var cookie = result.getResponse().getCookie("refreshToken");
		String rawRefresh = cookie.getValue();
		RefreshToken saved = refreshTokenRepository.findByUserId(user.getId()).orElseThrow();
		
		assertThat(saved.getTokenHash()).isEqualTo(refreshTokenHasher.hash(rawRefresh));
		assertThat(saved.getExpiresAt()).isNotNull();
	}
}
