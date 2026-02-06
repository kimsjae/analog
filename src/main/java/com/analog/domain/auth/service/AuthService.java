package com.analog.domain.auth.service;

import com.analog.domain.auth.dto.request.LoginRequest;
import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.AuthTokens;
import com.analog.domain.auth.dto.response.SignupResponse;

public interface AuthService {

	SignupResponse signup(SignupRequest request);
	
	AuthTokens login(LoginRequest request);
}
