package com.routinelog.user.service;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.user.domain.User;
import com.routinelog.user.dto.ProfileImageResponse;
import com.routinelog.user.dto.UpdateUserRequest;
import com.routinelog.user.dto.UserProfileResponse;
import com.routinelog.user.dto.UserUpdateResponse;
import com.routinelog.user.repository.UserRepository;
import com.routinelog.video.storage.VideoStorage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private static final Map<String, String> SUPPORTED_IMAGE_TYPES = Map.of(
		"image/jpeg", "jpg",
		"image/png", "png",
		"image/webp", "webp"
	);

	private final UserRepository userRepository;
	private final VideoStorage videoStorage;

	@Transactional(readOnly = true)
	public UserProfileResponse findMe(Long userId) {
		User user = findUser(userId);
		return UserProfileResponse.of(user, createProfileImageUrl(user));
	}

	@Transactional
	public UserUpdateResponse updateMe(Long userId, UpdateUserRequest request) {
		User user = findUser(userId);
		user.updateName(request.name());
		return UserUpdateResponse.of(user, createProfileImageUrl(user));
	}

	@Transactional
	public ProfileImageResponse updateProfileImage(Long userId, MultipartFile file) {
		User user = findUser(userId);
		String extension = validateImage(file);
		Path temporaryFile = copyToTemporaryFile(file, extension);
		String objectKey = "users/%d/profile-images/%s.%s".formatted(
			userId,
			UUID.randomUUID(),
			extension
		);
		boolean uploaded = false;
		try {
			videoStorage.upload(objectKey, temporaryFile, file.getContentType());
			uploaded = true;
			user.updateProfileImageKey(objectKey);
			userRepository.flush();
			return new ProfileImageResponse(videoStorage.createPlaybackUrl(objectKey));
		} catch (RuntimeException exception) {
			if (uploaded) {
				deleteUploadedImage(objectKey);
			}
			if (exception instanceof BusinessException businessException) {
				throw businessException;
			}
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		} finally {
			deleteTemporaryFile(temporaryFile);
		}
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private String createProfileImageUrl(User user) {
		return user.getProfileImageKey() == null
			? null
			: videoStorage.createPlaybackUrl(user.getProfileImageKey());
	}

	private String validateImage(MultipartFile file) {
		if (file == null || file.isEmpty() || file.getSize() <= 0) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		String extension = SUPPORTED_IMAGE_TYPES.get(file.getContentType());
		if (extension == null) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		return extension;
	}

	private Path copyToTemporaryFile(MultipartFile file, String extension) {
		Path temporaryFile = null;
		try {
			temporaryFile = Files.createTempFile("profile-image-", "." + extension);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
			}
			return temporaryFile;
		} catch (IOException exception) {
			if (temporaryFile != null) {
				deleteTemporaryFile(temporaryFile);
			}
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private void deleteUploadedImage(String objectKey) {
		try {
			videoStorage.delete(objectKey);
		} catch (RuntimeException exception) {
			log.error("Failed to clean up profile image object: {}", objectKey, exception);
		}
	}

	private void deleteTemporaryFile(Path temporaryFile) {
		try {
			Files.deleteIfExists(temporaryFile);
		} catch (IOException exception) {
			log.warn("Failed to delete profile image temporary file: {}", temporaryFile, exception);
		}
	}
}
