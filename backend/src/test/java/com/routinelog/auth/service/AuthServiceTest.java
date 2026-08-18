package com.routinelog.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.routinelog.auth.dto.LoginRequest;
import com.routinelog.auth.dto.LoginResponse;
import com.routinelog.auth.dto.SignupRequest;
import com.routinelog.auth.dto.SignupResponse;
import com.routinelog.auth.jwt.JwtTokenProvider;
import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import java.util.Optional;
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

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
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

	@Test
	void loginReturnsBearerAccessToken() {
		LoginRequest request = new LoginRequest("user@example.com", "password123!");
		User user = new User(request.email(), "encoded-password", "User");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
		when(jwtTokenProvider.createAccessToken(user.getId())).thenReturn("access-token");

		LoginResponse response = authService.login(request);

		assertEquals("access-token", response.accessToken());
		assertEquals("Bearer", response.tokenType());
	}

	@Test
	void loginRejectsUnknownEmail() {
		LoginRequest request = new LoginRequest("unknown@example.com", "password123!");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> authService.login(request)
		);

		assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
		verify(passwordEncoder, never()).matches(any(), any());
		verify(jwtTokenProvider, never()).createAccessToken(any());
	}

	@Test
	void loginRejectsWrongPassword() {
		LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
		User user = new User(request.email(), "encoded-password", "User");
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> authService.login(request)
		);

		assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
		verify(jwtTokenProvider, never()).createAccessToken(any());
	}
}
