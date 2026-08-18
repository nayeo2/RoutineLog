package com.routinelog.auth.service;

import com.routinelog.auth.dto.LoginRequest;
import com.routinelog.auth.dto.LoginResponse;
import com.routinelog.auth.dto.SignupRequest;
import com.routinelog.auth.dto.SignupResponse;
import com.routinelog.auth.jwt.JwtTokenProvider;
import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		String encodedPassword = passwordEncoder.encode(request.password());
		User user = new User(request.email(), encodedPassword, request.name());
		User savedUser = userRepository.save(user);

		return SignupResponse.from(savedUser);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		return LoginResponse.bearer(jwtTokenProvider.createAccessToken(user.getId()));
	}
}
