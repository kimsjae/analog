package com.analog.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.cookie.refresh")
public record RefreshCookieProperties(
		boolean secure,
		String hmacSecret
) {

}
