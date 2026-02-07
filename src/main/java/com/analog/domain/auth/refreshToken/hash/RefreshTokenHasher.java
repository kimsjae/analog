package com.analog.domain.auth.refreshToken.hash;

public interface RefreshTokenHasher {

	String hash(String rawToken);
}
