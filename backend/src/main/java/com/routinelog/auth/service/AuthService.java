package com.routinelog.auth.service;

import com.routinelog.auth.dto.SignupRequest;
import com.routinelog.auth.dto.SignupResponse;
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
}
