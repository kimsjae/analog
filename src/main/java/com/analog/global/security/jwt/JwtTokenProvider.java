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
import java.util.UUID;

public class JwtTokenProvider {

    private final JwtProperties props;
    private final Clock clock;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    public String createAccessToken(long userId) {
        return createToken(userId, TokenType.ACCESS, props.accessTokenExpSeconds());
    }

    public String createRefreshToken(long userId) {
        return createToken(userId, TokenType.REFRESH, props.refreshTokenExpSeconds());
    }

    private String createToken(long userId, TokenType type, long expSeconds) {
        Instant now = clock.instant();
        Instant exp = now.plusSeconds(expSeconds);

        return Jwts.builder()
        		.id(UUID.randomUUID().toString())
                .issuer(props.issuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("typ", type.name())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public JwtClaims parse(String token) {
        Claims claims = parseClaims(token);

        Long userId = Long.parseLong(claims.getSubject());
        String typ = claims.get("typ", String.class);
        if (typ == null) {
        	throw new JwtException("Missing token type");
        }
        TokenType tokenType = TokenType.valueOf(typ);
        String tokenId = claims.getId();
        Date exp = claims.getExpiration();
        Instant expiresAt = (exp == null) ? null : exp.toInstant();

        return new JwtClaims(userId, tokenType, tokenId, expiresAt);
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
