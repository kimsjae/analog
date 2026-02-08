package com.analog.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analog.domain.auth.dto.request.LoginRequest;
import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.LoginBody;
import com.analog.domain.auth.dto.response.LoginResponse;
import com.analog.domain.auth.dto.response.ReissueResponse;
import com.analog.domain.auth.dto.response.SignupResponse;
import com.analog.domain.auth.refreshToken.entity.RefreshToken;
import com.analog.domain.auth.refreshToken.hash.RefreshTokenHasher;
import com.analog.domain.auth.refreshToken.repository.RefreshTokenRepository;
import com.analog.domain.auth.refreshToken.service.RefreshTokenService;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.error.BusinessException;
import com.analog.global.error.ErrorCode;
import com.analog.global.security.jwt.JwtClaims;
import com.analog.global.security.jwt.JwtTokenProvider;
import com.analog.global.security.jwt.TokenType;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final RefreshTokenRepository refreshTokenRepository;
	private final RefreshTokenHasher refreshTokenHasher;
	
	@Override
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.RES_409, "이미 사용 중인 이메일입니다.");
		}
		
		String encoded = passwordEncoder.encode(request.password());
		User user = userRepository.save(
				User.createLocal(request.email(), encoded, request.name())
		);
		
		return new SignupResponse(user.getId(), user.getEmail(), user.getName());
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_401));
		
		boolean matched = passwordEncoder.matches(request.password(), user.getPassword());
		if (!matched) {
			throw new BusinessException(ErrorCode.AUTH_401);
		}
		
		String accessToken = jwtTokenProvider.createAccessToken(user.getId());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
		
		JwtClaims refreshClaims = jwtTokenProvider.parse(refreshToken);
		refreshTokenService.upsert(user, refreshToken, refreshClaims.tokenId(), refreshClaims.expiresAt());
		
		LoginBody response = new LoginBody(user.getId(), user.getEmail(), user.getName(), accessToken);
		
		return new LoginResponse(response, refreshToken);
	}
	
	@Override
	public ReissueResponse reissue(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new BusinessException(ErrorCode.AUTH_401);
		}
		
		JwtClaims refreshClaims;
		try {
			refreshClaims = jwtTokenProvider.parse(rawRefreshToken);
		} catch (ExpiredJwtException e) {
			throw new BusinessException(ErrorCode.AUTH_401);
		} catch (JwtException e) {
			throw new BusinessException(ErrorCode.AUTH_401);
		}
		
		if (refreshClaims.tokenType() != TokenType.REFRESH) {
			throw new BusinessException(ErrorCode.AUTH_401);
		}
		
		Long userId = refreshClaims.userId();
		String tokenId = refreshClaims.tokenId();
		
		RefreshToken stored = refreshTokenRepository.findByUserId(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_401));
		
		String hashed = refreshTokenHasher.hash(rawRefreshToken);
		if (!stored.getTokenHash().equals(hashed) || !stored.getTokenId().equals(tokenId)) {
			throw new BusinessException(ErrorCode.AUTH_401);
		}
		
		String newAccessToken = jwtTokenProvider.createAccessToken(userId);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
		
		JwtClaims newRefreshClaims = jwtTokenProvider.parse(newRefreshToken);
		
		refreshTokenService.upsert(stored.getUser(), newRefreshToken, newRefreshClaims.tokenId(), newRefreshClaims.expiresAt());
		
		return new ReissueResponse(newAccessToken, newRefreshToken);
	}
}
