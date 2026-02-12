package com.analog.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.analog.domain.auth.refreshToken.repository.RefreshTokenRepository;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.security.jwt.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    PasswordEncoder passwordEncoder;
    
    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }
    
    @Test
    void me_success_200() throws Exception {
    	User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
    	String accessToken = jwtTokenProvider.createAccessToken(user.getId());
    	
    	mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.userId").value(user.getId()))
               .andExpect(jsonPath("$.email").value("test@test.com"))
               .andExpect(jsonPath("$.name").value("tester"));
    }
    
    @Test
    void me_fails_401() throws Exception {
    	mockMvc.perform(get("/api/users/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.errorCode").value("AUTH_401"))
        .andExpect(jsonPath("$.path").value("/api/users/me"));
    	
    	mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.errorCode").value("AUTH_401"))
               .andExpect(jsonPath("$.path").value("/api/users/me"));
    	
    	User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
    	String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
    	
    	mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.errorCode").value("AUTH_401"))
               .andExpect(jsonPath("$.path").value("/api/users/me"));
    }
}
