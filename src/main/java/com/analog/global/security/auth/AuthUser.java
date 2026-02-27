package com.analog.global.security.auth;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.analog.domain.user.entity.User;
import com.analog.global.error.BusinessException;
import com.analog.global.error.ErrorCode;

public final class AuthUser {

	private AuthUser() {}
	
	public static Optional<User> getUser() {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    if (auth == null || !auth.isAuthenticated()
	            || auth instanceof AnonymousAuthenticationToken) {
	        return Optional.empty();
	    }

	    Object principal = auth.getPrincipal();
	    if (principal instanceof User user) {
	        return Optional.of(user);
	    }

	    return Optional.empty();
	}

	public static User requireUser() {
	    return getUser()
	            .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_401));
	}
}
