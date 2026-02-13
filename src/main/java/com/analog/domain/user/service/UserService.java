package com.analog.domain.user.service;

import com.analog.domain.user.dto.request.UpdateMeRequest;
import com.analog.domain.user.dto.request.UpdatePasswordRequest;
import com.analog.domain.user.dto.response.MeResponse;
import com.analog.domain.user.dto.response.UpdatePasswordResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

	MeResponse me();
	
	MeResponse updateMe(UpdateMeRequest request);
	
	UpdatePasswordResponse updatePassword(UpdatePasswordRequest request);
	
	void withdraw(Long userId, String password, HttpServletResponse response);
}
