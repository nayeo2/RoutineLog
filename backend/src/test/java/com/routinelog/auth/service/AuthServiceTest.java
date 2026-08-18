package com.routinelog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.routinelog.auth.dto.SignupRequest;
import com.routinelog.auth.dto.SignupResponse;
import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, passwordEncoder);
	}

	@Test
	void signupEncodesPasswordAndSavesUser() {
		SignupRequest request = new SignupRequest("user@example.com", "password123!", "User");
		when(userRepository.existsByEmail(request.email())).thenReturn(false);
		when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		SignupResponse response = authService.signup(request);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		assertEquals("encoded-password", userCaptor.getValue().getPassword());
		assertEquals(request.email(), response.email());
		assertEquals(request.name(), response.name());
	}

	@Test
	void signupRejectsDuplicateEmail() {
		SignupRequest request = new SignupRequest("user@example.com", "password123!", "User");
		when(userRepository.existsByEmail(request.email())).thenReturn(true);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> authService.signup(request)
		);

		assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
	}
}
