package com.routinelog.video.service;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import com.routinelog.dailyroutine.repository.DailyRoutineRepository;
import com.routinelog.video.domain.Video;
import com.routinelog.video.dto.VideoResponse;
import com.routinelog.video.dto.VideoUploadResponse;
import com.routinelog.video.metadata.VideoMetadataExtractor;
import com.routinelog.video.repository.VideoRepository;
import com.routinelog.video.storage.VideoStorage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class VideoService {

	private static final BigDecimal MAX_DURATION_SECONDS = BigDecimal.valueOf(15);
	private static final String SUPPORTED_CONTENT_TYPE = "video/mp4";

	private final DailyRoutineRepository dailyRoutineRepository;
	private final VideoRepository videoRepository;
	private final VideoStorage videoStorage;
	private final VideoMetadataExtractor metadataExtractor;
	private final long maxFileSizeBytes;

	public VideoService(
		DailyRoutineRepository dailyRoutineRepository,
		VideoRepository videoRepository,
		VideoStorage videoStorage,
		VideoMetadataExtractor metadataExtractor,
		@Value("${video.max-file-size-bytes}") long maxFileSizeBytes
	) {
		this.dailyRoutineRepository = dailyRoutineRepository;
		this.videoRepository = videoRepository;
		this.videoStorage = videoStorage;
		this.metadataExtractor = metadataExtractor;
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	@Transactional
	public VideoUploadResponse upload(Long userId, Long dailyRoutineId, MultipartFile file) {
		DailyRoutine dailyRoutine = findOwnedDailyRoutine(userId, dailyRoutineId);
		if (videoRepository.existsByDailyRoutineId(dailyRoutineId)) {
			throw new BusinessException(ErrorCode.VIDEO_ALREADY_EXISTS);
		}
		if (dailyRoutine.getStatus() != RoutineStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_DAILY_ROUTINE_STATUS);
		}
		validateFile(file);

		Path temporaryFile = copyToTemporaryFile(file);
		String objectKey = createObjectKey(userId, dailyRoutine.getRoutineDate());
		boolean uploaded = false;
		try {
			BigDecimal durationSeconds = metadataExtractor.extractDuration(temporaryFile);
			validateDuration(durationSeconds);
			BigDecimal storedDurationSeconds = durationSeconds.setScale(2, RoundingMode.HALF_UP);
			if (storedDurationSeconds.signum() <= 0) {
				throw new BusinessException(ErrorCode.INVALID_VIDEO_FILE);
			}
			videoStorage.upload(objectKey, temporaryFile, SUPPORTED_CONTENT_TYPE);
			uploaded = true;

			Video video = new Video(
				dailyRoutine,
				objectKey,
				cleanOriginalFilename(file.getOriginalFilename()),
				SUPPORTED_CONTENT_TYPE,
				storedDurationSeconds,
				file.getSize()
			);
			Video savedVideo = videoRepository.saveAndFlush(video);
			dailyRoutine.succeed();
			dailyRoutineRepository.flush();
			return VideoUploadResponse.of(savedVideo, dailyRoutine);
		} catch (RuntimeException exception) {
			if (uploaded) {
				deleteUploadedObject(objectKey);
			}
			if (exception instanceof BusinessException businessException) {
				throw businessException;
			}
			if (exception instanceof DataIntegrityViolationException) {
				throw new BusinessException(ErrorCode.VIDEO_ALREADY_EXISTS);
			}
			throw new BusinessException(ErrorCode.VIDEO_UPLOAD_FAILED);
		} finally {
			deleteTemporaryFile(temporaryFile);
		}
	}

	@Transactional(readOnly = true)
	public VideoResponse find(Long userId, Long videoId) {
		Video video = videoRepository.findById(videoId)
			.orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
		if (!Objects.equals(video.getDailyRoutine().getUser().getId(), userId)) {
			throw new BusinessException(ErrorCode.VIDEO_ACCESS_DENIED);
		}
		return VideoResponse.of(video, videoStorage.createPlaybackUrl(video.getObjectKey()));
	}

	private DailyRoutine findOwnedDailyRoutine(Long userId, Long dailyRoutineId) {
		DailyRoutine dailyRoutine = dailyRoutineRepository.findById(dailyRoutineId)
			.orElseThrow(() -> new BusinessException(ErrorCode.DAILY_ROUTINE_NOT_FOUND));
		if (!Objects.equals(dailyRoutine.getUser().getId(), userId)) {
			throw new BusinessException(ErrorCode.DAILY_ROUTINE_ACCESS_DENIED);
		}
		return dailyRoutine;
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty() || file.getSize() <= 0) {
			throw new BusinessException(ErrorCode.INVALID_VIDEO_FILE);
		}
		if (file.getSize() > maxFileSizeBytes) {
			throw new BusinessException(ErrorCode.VIDEO_TOO_LARGE);
		}
		if (!SUPPORTED_CONTENT_TYPE.equalsIgnoreCase(file.getContentType())) {
			throw new BusinessException(ErrorCode.INVALID_VIDEO_FILE);
		}
	}

	private void validateDuration(BigDecimal durationSeconds) {
		if (durationSeconds == null || durationSeconds.signum() <= 0) {
			throw new BusinessException(ErrorCode.INVALID_VIDEO_FILE);
		}
		if (durationSeconds.compareTo(MAX_DURATION_SECONDS) > 0) {
			throw new BusinessException(ErrorCode.VIDEO_TOO_LONG);
		}
	}

	private Path copyToTemporaryFile(MultipartFile file) {
		Path temporaryFile = null;
		try {
			temporaryFile = Files.createTempFile("routine-video-", ".mp4");
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
			}
			return temporaryFile;
		} catch (IOException exception) {
			if (temporaryFile != null) {
				deleteTemporaryFile(temporaryFile);
			}
			throw new BusinessException(ErrorCode.VIDEO_UPLOAD_FAILED);
		}
	}

	private String createObjectKey(Long userId, LocalDate routineDate) {
		return "users/%d/routine-videos/%04d/%02d/%02d/%s.mp4".formatted(
			userId,
			routineDate.getYear(),
			routineDate.getMonthValue(),
			routineDate.getDayOfMonth(),
			UUID.randomUUID()
		);
	}

	private String cleanOriginalFilename(String originalFilename) {
		if (!StringUtils.hasText(originalFilename)) {
			return null;
		}
		String filename = StringUtils.cleanPath(originalFilename);
		int separatorIndex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
		filename = filename.substring(separatorIndex + 1);
		return filename.length() <= 255 ? filename : filename.substring(filename.length() - 255);
	}

	private void deleteUploadedObject(String objectKey) {
		try {
			videoStorage.delete(objectKey);
		} catch (RuntimeException cleanupException) {
			log.error("Failed to clean up S3 object after video upload failure: {}", objectKey,
				cleanupException);
		}
	}

	private void deleteTemporaryFile(Path temporaryFile) {
		try {
			Files.deleteIfExists(temporaryFile);
		} catch (IOException exception) {
			log.warn("Failed to delete temporary video file: {}", temporaryFile, exception);
		}
	}
}
