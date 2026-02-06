package com.analog.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import com.analog.global.config.JwtProperties;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

public class JwtTokenProvider {

    private final JwtProperties props;
    private final Clock clock;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    public String createAccessToken(long userId, String email, String name) {
        return createToken(userId, email, name, TokenType.ACCESS, props.accessTokenExpSeconds());
    }

    public String createRefreshToken(long userId, String email, String name) {
        return createToken(userId, email, name, TokenType.REFRESH, props.refreshTokenExpSeconds());
    }

    private String createToken(long userId, String email, String name, TokenType type, long expSeconds) {
        Instant now = clock.instant();
        Instant exp = now.plusSeconds(expSeconds);

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("email", email)
                .claim("name", name)
                .claim("typ", type.name())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public void validateOrThrow(String token, TokenType expectedType) {
        Claims claims = parseClaims(token);

        if (expectedType != null) {
            String typ = claims.get("typ", String.class);
            if (typ == null || !expectedType.name().equals(typ)) {
                throw new JwtException("Invalid token type");
            }
        }
    }

    public JwtClaims parse(String token) {
        Claims claims = parseClaims(token);

        long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        String typ = claims.get("typ", String.class);

        return new JwtClaims(userId, email, name, TokenType.valueOf(typ));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
