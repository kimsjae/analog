package com.analog.global.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            handleAuthException(request, response, new BadCredentialsException("Invalid Authorization header"));
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            handleAuthException(request, response, new BadCredentialsException("Empty bearer token"));
            return;
        }

        try {
            JwtClaims claims = jwtTokenProvider.parse(token);

            if (claims.tokenType() != TokenType.ACCESS) {
                throw new BadCredentialsException("Token type mismatch");
            }

            Long userId = claims.userId();
            if (userId == null) {
                throw new BadCredentialsException("Missing userId");
            }

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());

            ((UsernamePasswordAuthenticationToken) authentication).setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            handleAuthException(request, response, new CredentialsExpiredException("Expired token", e));
        } catch (JwtException e) {
            handleAuthException(request, response, new BadCredentialsException("Invalid token", e));
        } catch (AuthenticationException e) {
            handleAuthException(request, response, e);
        }
    }

    private void handleAuthException(HttpServletRequest request,
                                     HttpServletResponse response,
                                     AuthenticationException ex) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, ex);
    }
}
