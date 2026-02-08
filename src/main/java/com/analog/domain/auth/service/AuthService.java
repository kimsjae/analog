package com.analog.domain.auth.service;

import com.analog.domain.auth.dto.request.LoginRequest;
import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.LoginResponse;
import com.analog.domain.auth.dto.response.ReissueResponse;
import com.analog.domain.auth.dto.response.SignupResponse;

public interface AuthService {

	SignupResponse signup(SignupRequest request);
	
	LoginResponse login(LoginRequest request);
	
	ReissueResponse reissue(String rawRefreshToken);
	
	void logout(String rawRefreshToken);
}
