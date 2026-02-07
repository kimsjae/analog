package com.analog.domain.auth.refreshToken.hash;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HmacSha256RefreshTokenHasher implements RefreshTokenHasher {

	private final byte[] secretBytes;
	
	public HmacSha256RefreshTokenHasher(
			@Value("${auth.cookie.refresh.hmac-secret}")
			String secret
	) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("auth.refresh.hmac-secret must not be empty");
		}
		this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
	}
	
	@Override
	public String hash(String rawToken) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
			byte[] result = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
			
			return toHex(result);
		} catch (Exception e) {
			throw new IllegalStateException("HMAC-SHA256 hashing failed", e);
		}
	}
	
	private String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		
		return sb.toString();
	}
}
