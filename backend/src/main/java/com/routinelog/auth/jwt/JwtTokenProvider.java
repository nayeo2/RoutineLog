package com.routinelog.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final SecretKey secretKey;
	private final long accessTokenExpirationMs;

	public JwtTokenProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationMs = accessTokenExpirationMs;
	}

	public String createAccessToken(Long userId) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusMillis(accessTokenExpirationMs);

		return Jwts.builder()
			.subject(userId.toString())
			.issuedAt(Date.from(issuedAt))
			.expiration(Date.from(expiresAt))
			.signWith(secretKey)
			.compact();
	}

	public Long getUserId(String token) {
		String subject = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();

		return Long.valueOf(subject);
	}
}
