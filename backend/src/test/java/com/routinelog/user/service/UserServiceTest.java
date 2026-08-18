package com.routinelog.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.user.domain.User;
import com.routinelog.user.dto.ProfileImageResponse;
import com.routinelog.user.dto.UpdateUserRequest;
import com.routinelog.user.dto.UserProfileResponse;
import com.routinelog.user.dto.UserUpdateResponse;
import com.routinelog.user.repository.UserRepository;
import com.routinelog.video.storage.VideoStorage;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private VideoStorage videoStorage;

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService(userRepository, videoStorage);
	}

	@Test
	void findMeReturnsAuthenticatedUserWithAuthorizedProfileUrl() {
		User user = user(1L);
		user.updateProfileImageKey("users/1/profile-images/profile.jpg");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(videoStorage.createPlaybackUrl(user.getProfileImageKey()))
			.thenReturn("https://example.test/profile");

		UserProfileResponse response = userService.findMe(1L);

		assertEquals(1L, response.id());
		assertEquals("user@example.com", response.email());
		assertEquals("https://example.test/profile", response.profileImageUrl());
	}

	@Test
	void updateMeChangesOnlyAuthenticatedUsersName() {
		User user = user(1L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		UserUpdateResponse response = userService.updateMe(
			1L,
			new UpdateUserRequest("Updated User")
		);

		assertEquals("Updated User", response.name());
		assertEquals("user@example.com", response.email());
	}

	@Test
	void updateProfileImageStoresObjectKeyAndReturnsPresignedUrl() {
		User user = user(1L);
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"profile.png",
			"image/png",
			new byte[] {1, 2, 3}
		);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(videoStorage.createPlaybackUrl(any()))
			.thenReturn("https://example.test/profile");

		ProfileImageResponse response = userService.updateProfileImage(1L, file);

		verify(videoStorage).upload(
			argThat(key -> key.matches("users/1/profile-images/[0-9a-f-]+\\.png")),
			any(Path.class),
			eq("image/png")
		);
		verify(userRepository).flush();
		assertEquals("https://example.test/profile", response.profileImageUrl());
		assertTrue(user.getProfileImageKey().endsWith(".png"));
	}

	@Test
	void updateProfileImageRejectsUnsupportedFile() {
		User user = user(1L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"profile.txt",
			"text/plain",
			new byte[] {1}
		);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> userService.updateProfileImage(1L, file)
		);

		assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
		verify(videoStorage, never()).upload(any(), any(), any());
	}

	private User user(Long userId) {
		User user = new User("user@example.com", "encoded-password", "User");
		ReflectionTestUtils.setField(user, "id", userId);
		return user;
	}
}
