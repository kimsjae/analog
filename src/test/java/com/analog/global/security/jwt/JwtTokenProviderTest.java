package com.analog.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.analog.global.config.JwtProperties;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

class JwtTokenProviderTest {

	private static String base64Secret(String raw32bytes) {
		return Base64.getEncoder().encodeToString(raw32bytes.getBytes());
	}
	
	@Test
	void create_access_and_validate_parse() {
		Instant now = Instant.parse("2026-02-06T00:00:00Z");
        Clock fixed = Clock.fixed(now, ZoneOffset.UTC);

        String secret = base64Secret("01234567890123456789012345678901");
        JwtProperties props = new JwtProperties("analog", secret, 900, 1209600);
        JwtTokenProvider provider = new JwtTokenProvider(props, fixed);

        String token = provider.createAccessToken(1L);

        JwtClaims claims = provider.parse(token);
        assertThat(claims.userId()).isEqualTo(1L);
        assertThat(claims.tokenType()).isEqualTo(TokenType.ACCESS);
	}
	
	@Test
    void expired_token_throws() {
        Instant now = Instant.parse("2026-02-06T00:00:00Z");
        Clock fixed = Clock.fixed(now, ZoneOffset.UTC);

        String secret = base64Secret("01234567890123456789012345678901");
        JwtProperties props = new JwtProperties("analog", secret, 1, 2);

        JwtTokenProvider provider = new JwtTokenProvider(props, fixed);
        String token = provider.createAccessToken(1L);

        Clock later = Clock.fixed(now.plusSeconds(2), ZoneOffset.UTC);
        JwtTokenProvider laterProvider = new JwtTokenProvider(props, later);

        assertThatThrownBy(() -> laterProvider.parse(token))
        .isInstanceOf(ExpiredJwtException.class);

    }

    @Test
    void invalid_signature_throws() {
        Instant now = Instant.parse("2026-02-06T00:00:00Z");
        Clock fixed = Clock.fixed(now, ZoneOffset.UTC);

        String secretA = base64Secret("01234567890123456789012345678901");
        String secretB = base64Secret("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        JwtTokenProvider providerA = new JwtTokenProvider(new JwtProperties("analog", secretA, 900, 1209600), fixed);
        JwtTokenProvider providerB = new JwtTokenProvider(new JwtProperties("analog", secretB, 900, 1209600), fixed);

        String token = providerA.createAccessToken(1L);

        assertThatThrownBy(() -> providerB.parse(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void token_type_mismatch_throws() {
        Instant now = Instant.parse("2026-02-06T00:00:00Z");
        Clock fixed = Clock.fixed(now, ZoneOffset.UTC);

        String secret = base64Secret("01234567890123456789012345678901");
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties("analog", secret, 900, 1209600), fixed);

        String refresh = provider.createRefreshToken(1L);

        assertThatThrownBy(() -> provider.parse(refresh))
                .isInstanceOf(JwtException.class);
    }
}
