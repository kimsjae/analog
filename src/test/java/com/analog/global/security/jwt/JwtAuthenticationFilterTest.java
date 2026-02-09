package com.analog.global.security.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analog.domain.auth.refreshToken.repository.RefreshTokenRepository;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(JwtAuthenticationFilterTest.TestOnlyController.class)
class JwtAuthenticationFilterTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
    	refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void token_missing_on_protected_endpoint_returns_401() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void valid_access_token_allows_request_200() throws Exception {
        User user = userRepository.save(User.createLocal("t@test.com", "pw", "tester"));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        mockMvc.perform(get("/api/test/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
               .andExpect(status().isOk());
    }

    @Test
    void invalid_token_returns_401() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
               .andExpect(status().isUnauthorized());
    }

    @RestController
    @RequestMapping("/api/test")
    static class TestOnlyController {
        @GetMapping("/protected")
        ResponseEntity<String> protectedEndpoint() {
            return ResponseEntity.ok("ok");
        }
    }
}
