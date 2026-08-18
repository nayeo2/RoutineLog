package com.routinelog.auth.security;

import com.routinelog.auth.jwt.JwtTokenProvider;
import com.routinelog.common.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authorization = request.getHeader(AUTHORIZATION_HEADER);

		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = authorization.substring(BEARER_PREFIX.length());
			Long userId = jwtTokenProvider.getUserId(token);
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(userId, null, List.of());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException exception) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.writeResponse(response, ErrorCode.EXPIRED_TOKEN);
		} catch (JwtException | IllegalArgumentException exception) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.writeResponse(response, ErrorCode.INVALID_TOKEN);
		}
	}
}
