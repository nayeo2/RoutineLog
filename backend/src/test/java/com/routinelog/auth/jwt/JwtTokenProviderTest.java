package com.routinelog.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

	private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
		"test-jwt-secret-key-must-be-at-least-32-bytes",
		3_600_000
	);

	@Test
	void createsTokenContainingUserId() {
		String token = jwtTokenProvider.createAccessToken(1L);

		assertEquals(1L, jwtTokenProvider.getUserId(token));
	}
}
