package com.analog.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.analog.domain.user.dto.request.UpdateMeRequest;
import com.analog.domain.user.dto.response.MeResponse;
import com.analog.domain.user.entity.User;
import com.analog.domain.user.repository.UserRepository;
import com.analog.global.error.BusinessException;
import com.analog.global.error.ErrorCode;
import com.analog.global.security.auth.AuthUser;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	
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
}
