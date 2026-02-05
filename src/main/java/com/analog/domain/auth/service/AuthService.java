package com.analog.domain.auth.service;

import com.analog.domain.auth.dto.request.SignupRequest;
import com.analog.domain.auth.dto.response.SignupResponse;

public interface AuthService {

	SignupResponse signup(SignupRequest request);
}
