package com.routinelog.vlog.service;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import com.routinelog.video.domain.Video;
import com.routinelog.video.repository.VideoRepository;
import com.routinelog.video.storage.VideoStorage;
import com.routinelog.vlog.domain.DailyVlog;
import com.routinelog.vlog.domain.VlogStatus;
import com.routinelog.vlog.dto.DailyVlogResponse;
import com.routinelog.vlog.processing.VideoMerger;
import com.routinelog.vlog.repository.DailyVlogRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyVlogService {

	private final DailyVlogRepository dailyVlogRepository;
	private final VideoRepository videoRepository;
	private final UserRepository userRepository;
	private final VideoStorage videoStorage;
	private final VideoMerger videoMerger;

	public DailyVlogResponse create(Long userId, LocalDate vlogDate) {
		return dailyVlogRepository.findByUserIdAndVlogDate(userId, vlogDate)
			.map(this::toResponse)
			.orElseGet(() -> generate(userId, vlogDate));
	}

	public DailyVlogResponse findByDate(Long userId, LocalDate vlogDate) {
		DailyVlog dailyVlog = dailyVlogRepository.findByUserIdAndVlogDate(userId, vlogDate)
			.orElseThrow(() -> new BusinessException(ErrorCode.DAILY_VLOG_NOT_FOUND));
		return toResponse(dailyVlog);
	}

	private DailyVlogResponse generate(Long userId, LocalDate vlogDate) {
		List<Video> videos = videoRepository.findAllByUserIdAndRoutineDateAndStatus(
			userId,
			vlogDate,
			RoutineStatus.SUCCESS
		);
		if (videos.isEmpty()) {
			throw new BusinessException(ErrorCode.NO_VIDEOS_FOR_VLOG);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		DailyVlog dailyVlog = saveProcessingVlog(userId, new DailyVlog(user, vlogDate));
		if (dailyVlog.getStatus() != VlogStatus.PROCESSING) {
			return toResponse(dailyVlog);
		}

		Path workingDirectory = null;
		String objectKey = createObjectKey(userId, vlogDate);
		boolean uploaded = false;
		DailyVlog savedVlog;
		try {
			workingDirectory = createWorkingDirectory();
			List<Path> inputFiles = downloadVideos(videos, workingDirectory);
			Path outputFile = workingDirectory.resolve("daily-vlog.mp4");
			videoMerger.merge(inputFiles, outputFile);
			videoStorage.upload(objectKey, outputFile, "video/mp4");
			uploaded = true;

			dailyVlog.succeed(objectKey, totalDuration(videos));
			savedVlog = dailyVlogRepository.saveAndFlush(dailyVlog);
		} catch (RuntimeException exception) {
			if (uploaded) {
				deleteUploadedObject(objectKey);
			}
			markFailed(dailyVlog, exception);
			throw new BusinessException(ErrorCode.VLOG_GENERATION_FAILED);
		} finally {
			if (workingDirectory != null) {
				deleteWorkingDirectory(workingDirectory);
			}
		}
		return toResponse(savedVlog);
	}

	private DailyVlog saveProcessingVlog(Long userId, DailyVlog dailyVlog) {
		try {
			return dailyVlogRepository.saveAndFlush(dailyVlog);
		} catch (DataIntegrityViolationException exception) {
			DailyVlog existingVlog = dailyVlogRepository
				.findByUserIdAndVlogDate(userId, dailyVlog.getVlogDate())
				.orElseThrow(() -> new BusinessException(ErrorCode.VLOG_ALREADY_PROCESSING));
			if (existingVlog.getStatus() == VlogStatus.PROCESSING) {
				throw new BusinessException(ErrorCode.VLOG_ALREADY_PROCESSING);
			}
			return existingVlog;
		}
	}

	private List<Path> downloadVideos(List<Video> videos, Path workingDirectory) {
		List<Path> inputFiles = new ArrayList<>();
		for (int index = 0; index < videos.size(); index++) {
			Path destination = workingDirectory.resolve("%03d.mp4".formatted(index));
			videoStorage.download(videos.get(index).getObjectKey(), destination);
			inputFiles.add(destination);
		}
		return inputFiles;
	}

	private BigDecimal totalDuration(List<Video> videos) {
		return videos.stream()
			.map(Video::getDurationSeconds)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(2, RoundingMode.HALF_UP);
	}

	private DailyVlogResponse toResponse(DailyVlog dailyVlog) {
		String playbackUrl = dailyVlog.getStatus() == VlogStatus.SUCCESS
			? videoStorage.createPlaybackUrl(dailyVlog.getObjectKey())
			: null;
		return DailyVlogResponse.of(dailyVlog, playbackUrl);
	}

	private Path createWorkingDirectory() {
		try {
			return Files.createTempDirectory("daily-vlog-");
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.VLOG_GENERATION_FAILED);
		}
	}

	private String createObjectKey(Long userId, LocalDate vlogDate) {
		return "users/%d/daily-vlogs/%04d/%02d/%s-%s.mp4".formatted(
			userId,
			vlogDate.getYear(),
			vlogDate.getMonthValue(),
			vlogDate,
			UUID.randomUUID()
		);
	}

	private void markFailed(DailyVlog dailyVlog, RuntimeException exception) {
		try {
			dailyVlog.fail(exception.getClass().getSimpleName());
			dailyVlogRepository.saveAndFlush(dailyVlog);
		} catch (RuntimeException persistenceException) {
			log.error("Failed to persist DailyVlog failure state: {}", dailyVlog.getId(),
				persistenceException);
		}
	}

	private void deleteUploadedObject(String objectKey) {
		try {
			videoStorage.delete(objectKey);
		} catch (RuntimeException exception) {
			log.error("Failed to clean up DailyVlog S3 object: {}", objectKey, exception);
		}
	}

	private void deleteWorkingDirectory(Path workingDirectory) {
		try (var paths = Files.walk(workingDirectory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException exception) {
					log.warn("Failed to delete DailyVlog temporary file: {}", path, exception);
				}
			});
		} catch (IOException exception) {
			log.warn("Failed to clean DailyVlog temporary directory: {}", workingDirectory, exception);
		}
	}
}
