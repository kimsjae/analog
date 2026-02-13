package com.analog.domain.user.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analog.domain.auth.refreshToken.repository.RefreshTokenRepository;
import com.analog.domain.auth.service.RefreshTokenService;
import com.analog.domain.diary.analysis.repository.DiaryAnalysisRepository;
import com.analog.domain.diary.repository.DiaryRepository;
import com.analog.domain.user.dto.request.UpdateMeRequest;
import com.analog.domain.user.dto.request.UpdatePasswordRequest;
import com.analog.domain.user.dto.response.MeResponse;
import com.analog.domain.user.dto.response.UpdatePasswordResponse;
import com.analog.domain.user.entity.AuthProvider;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.error.BusinessException;
import com.analog.global.error.ErrorCode;
import com.analog.global.security.auth.AuthUser;
import com.analog.global.security.jwt.JwtClaims;
import com.analog.global.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final DiaryAnalysisRepository diaryAnalysisRepository;
	private final DiaryRepository diaryRepository;
	
	@Override
	@Transactional(readOnly = true)
	public MeResponse me() {
		Long userId = AuthUser.requireUserId();
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RES_404, "사용자를 찾을 수 없습니다."));
		
		return new MeResponse(user.getId(), user.getEmail(), user.getName());
	}
	
	@Override
	public MeResponse updateMe(UpdateMeRequest request) {
		Long userId = AuthUser.requireUserId();
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RES_404, "사용자를 찾을 수 없습니다."));
		
		user.updateName(request.name());
		
		return new MeResponse(user.getId(), user.getEmail(), user.getName());
	}
	
	@Override
	public UpdatePasswordResponse updatePassword(UpdatePasswordRequest request) {
		Long userId = AuthUser.requireUserId();
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.RES_404));
		
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.REQ_400);
        }

        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new BusinessException(ErrorCode.REQ_400);
        }
        
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
        	throw new BusinessException(ErrorCode.REQ_400);
        }
        
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        
        JwtClaims newRefreshClaims = jwtTokenProvider.parse(newRefreshToken);
        
        refreshTokenService.upsert(user, newRefreshToken, newRefreshClaims.tokenId(), newRefreshClaims.expiresAt());
        
        return new UpdatePasswordResponse(newAccessToken, newRefreshToken);
	}
	
	@Override
	public void withdraw(Long userId, String password, HttpServletResponse response) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_401));
		
		if (user.getProvider() == AuthProvider.LOCAL) {
			if (!passwordEncoder.matches(password, user.getPassword())) {
				throw new BusinessException(ErrorCode.AUTH_401);
			}
		}
		
		refreshTokenRepository.deleteByUserId(userId);
		expireRefreshCookie(response);
		
		diaryAnalysisRepository.deleteByDiaryUserId(userId);
		diaryRepository.deleteByUserId(userId);
		
		userRepository.delete(user);
	}
	
	private void expireRefreshCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
				.httpOnly(true)
				.path("/api/auth")
				.maxAge(0)
				.sameSite("Lax")
				.build();
		
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
