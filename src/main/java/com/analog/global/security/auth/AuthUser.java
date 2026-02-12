package com.analog.global.security.auth;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.analog.global.error.BusinessException;
import com.analog.global.error.ErrorCode;

public final class AuthUser {

	private AuthUser() {}
	
	public static Optional<Long> getUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
            return Optional.empty();
        }

        if (!auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
		
		Object principal = auth.getPrincipal();
		if (principal instanceof Long userId) {
			return Optional.of(userId);
		}
		
		return Optional.empty();
	}
	
	public static Long requireUserId() {
		return getUserId().orElseThrow(() -> new BusinessException(ErrorCode.AUTH_401));
	}
}
