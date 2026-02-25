package com.analog.domain.diary.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.analog.domain.diary.entity.Diary;
import com.analog.domain.diary.repository.DiaryRepository;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.security.jwt.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
public class DiaryControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
    DiaryRepository diaryRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@Test
	void created_diary_success_201() throws Exception {
		User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));
		
		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		
		String requestBody = """
				{
				"title": "제목",
				"content": "내용",
				"diaryDate": "2026-02-25"
				}
				""";
		
		mockMvc.perform(post("/api/diaries")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isCreated());
		
		List<Diary> diaries = diaryRepository.findAll();
	    assertThat(diaries).hasSize(1);

	    Diary saved = diaries.get(0);
	    assertThat(saved.getUser().getId()).isEqualTo(user.getId());
	    assertThat(saved.getTitle()).isEqualTo("제목");
	    assertThat(saved.getContent()).isEqualTo("내용");
	    assertThat(saved.getDiaryDate()).isEqualTo(LocalDate.of(2026, 2, 25));
	}
	
	@Test
	void created_diary_fali() throws Exception {
		User user = userRepository.save(User.createLocal("test@test.com", passwordEncoder.encode("123123"), "tester"));

	    String accessToken = jwtTokenProvider.createAccessToken(user.getId());

	    String requestBody = """
	            {
	                "title": "",
	                "content": "",
	                "diaryDate": null
	            }
	            """;

	    mockMvc.perform(post("/api/diaries")
	                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
	                    .contentType(MediaType.APPLICATION_JSON)
	                    .content(requestBody))
	            .andExpect(status().isBadRequest());	
	}
}
