package com.analog.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.analog.domain.auth.refreshToken.repository.RefreshTokenRepository;
import com.analog.domain.auth.service.RefreshTokenService;
import com.analog.domain.diary.analysis.entity.AnalysisStatus;
import com.analog.domain.diary.analysis.entity.DiaryAnalysis;
import com.analog.domain.diary.analysis.repository.DiaryAnalysisRepository;
import com.analog.domain.diary.entity.Diary;
import com.analog.domain.diary.repository.DiaryRepository;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.security.jwt.JwtClaims;
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
    
    @Autowired
    RefreshTokenService refreshTokenService;
    
    @Autowired
    DiaryRepository diaryRepository;
    
    @Autowired
    DiaryAnalysisRepository diaryAnalysisRepository;
    
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
    
    @Test
    void update_me_success_200() throws Exception {
    	User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
    	String accessToken = jwtTokenProvider.createAccessToken(user.getId());
    	
    	mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content("""
                        { "name": "newName" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.name").value("newName"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("newName");
    }
    
    @Test
    void update_me_fails() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "newName" }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_401"))
                .andExpect(jsonPath("$.path").value("/api/users/me"));

        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .content("""
                        { "name": "newName" }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_401"))
                .andExpect(jsonPath("$.path").value("/api/users/me"));

        User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                .content("""
                        { "name": "newName" }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_401"))
                .andExpect(jsonPath("$.path").value("/api/users/me"));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content("""
                        { "name": "" }
                        """))
                .andExpect(status().isBadRequest());

        Long notExistsUserId = 2L;
        String tokenForNotExists = jwtTokenProvider.createAccessToken(notExistsUserId);

        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenForNotExists)
                .content("""
                        { "name": "newName" }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/users/me"))
                .andExpect(jsonPath("$.errorCode").value("RES_404"));
    }
    
    @Test
    void update_password_success() throws Exception {
    	User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
    	String accessToken = jwtTokenProvider.createAccessToken(user.getId());
    	String oldRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        JwtClaims oldClaims = jwtTokenProvider.parse(oldRefreshToken);
        refreshTokenService.upsert(user, oldRefreshToken, oldClaims.tokenId(), oldClaims.expiresAt());
        
        MvcResult result = mockMvc.perform(patch("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content("""
                    {
                      "currentPassword": "123123",
                      "newPassword": "456456",
                      "newPasswordConfirm": "456456"
                    }
                    """))
        // then
        .andExpect(status().isOk())
        .andReturn();
        
        String newAccessToken = result.getResponse().getContentAsString();
        assertThat(newAccessToken).isNotBlank();

        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("refreshToken=");

        String newRefreshToken = setCookie.split(";")[0].split("=")[1];
        assertThat(newRefreshToken).isNotBlank();

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("456456", updated.getPassword())).isTrue();
        
        MockCookie oldCookie = new MockCookie("refreshToken", oldRefreshToken);
        mockMvc.perform(post("/api/auth/reissue").cookie(oldCookie))
                .andExpect(status().isUnauthorized());

        MockCookie newCookie = new MockCookie("refreshToken", newRefreshToken);
        mockMvc.perform(post("/api/auth/reissue").cookie(newCookie))
                .andExpect(status().isOk());
    }
    
    @Test
    void update_password_fails() throws Exception {
    	mockMvc.perform(patch("/api/users/me/password")
    			.contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "currentPassword": "123123",
                          "newPassword": "456456",
                          "newPasswordConfirm": "456456"
                        }
                        """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.path").value("/api/users/me/password"));
    	
    	mockMvc.perform(patch("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .content("""
                    {
                      "currentPassword": "123123",
                      "newPassword": "456456",
                      "newPasswordConfirm": "456456"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.path").value("/api/users/me/password"));
    	
    	User user = userRepository.save(
                User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester")
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                        .content("""
                            {
                              "currentPassword": "123123",
                              "newPassword": "456456",
                              "newPasswordConfirm": "456456"
                            }
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.path").value("/api/users/me/password"));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                            {
                              "currentPassword": "123123213",
                              "newPassword": "456456",
                              "newPasswordConfirm": "456456"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/users/me/password"));

        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                            {
                              "currentPassword": "123123",
                              "newPassword": "456456",
                              "newPasswordConfirm": "456456456"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/users/me/password"));


        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .content("""
                            {
                              "currentPassword": "123123",
                              "newPassword": "123123",
                              "newPasswordConfirm": "123123"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/users/me/password"));

        Long notExistsUserId = 2L;
        String tokenForNotExists = jwtTokenProvider.createAccessToken(notExistsUserId);

        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenForNotExists)
                        .content("""
                            {
                              "currentPassword": "123123",
                              "newPassword": "456456",
                              "newPasswordConfirm": "456456"
                            }
                            """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/users/me/password"));
    }
    
    @Test
    void withdraw_success_204() throws Exception {
    	User user = userRepository.save(
                User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester")
        );

        Diary d1 = diaryRepository.save(Diary.create(user, "t1", "c1", java.time.LocalDate.now()));
        Diary d2 = diaryRepository.save(Diary.create(user, null, "c2", java.time.LocalDate.now().minusDays(1)));

        diaryAnalysisRepository.save(
                DiaryAnalysis.create(d1, AnalysisStatus.SUCCESS, java.time.LocalDateTime.now())
        );
        
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        
        mockMvc.perform(delete("/api/users/me")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                		{
                		"password": "123123"
                		}
                		"""))
        .andExpect(status().isNoContent())
        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refreshToken=")))
        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
    
    @Test
    void withdraw_fails() throws Exception {
        User user = userRepository.save(
                User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester")
        );
        String validAccessToken = jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(delete("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        		{
                        		"password": "123123"
                        		}
                        		"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").exists());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer invalid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        		{
                        		"password": "123123"
                        		}
                        		"""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").exists());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + validAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        		{
                        		"password": "456456"
                        		}
                        		"""))
                .andExpect(status().isUnauthorized());

        userRepository.delete(user);

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + validAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        		{
                        		"password": "123123"
                        		}
                        		"""))
                .andExpect(status().isUnauthorized());
    }
}
