package com.analog.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analog.domain.auth.dto.request.LoginRequest;
import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.AuthTokens;
import com.analog.domain.auth.dto.response.LoginResponse;
import com.analog.domain.auth.dto.response.SignupResponse;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.error.BusinessException;
import com.analog.global.error.ErrorCode;
import com.analog.global.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
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
	
	@Transactional(readOnly = true)
	@Override
	public AuthTokens login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_401));
		
		boolean matched = passwordEncoder.matches(request.password(), user.getPassword());
		
		if (!matched) {
			throw new BusinessException(ErrorCode.AUTH_401);
		}
		
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getName());
		
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getName());
		
		LoginResponse response = new LoginResponse(
				user.getId(),
				user.getEmail(),
				user.getName(),
				accessToken
		);
		
		return new AuthTokens(response, refreshToken);
	}
}
