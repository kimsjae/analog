package com.analog.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analog.domain.auth.dto.request.LoginRequest;
import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.LoginBody;
import com.analog.domain.auth.dto.response.LoginResponse;
import com.analog.domain.auth.dto.response.SignupResponse;
import com.analog.domain.auth.refreshToken.service.RefreshTokenService;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.error.BusinessException;
import com.analog.global.error.ErrorCode;
import com.analog.global.security.jwt.JwtClaims;
import com.analog.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	
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
	
	@Transactional
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
		
		LoginBody response = new LoginBody(
				user.getId(),
				user.getEmail(),
				user.getName(),
				accessToken
		);
		
		return new LoginResponse(response, refreshToken);
	}
}
