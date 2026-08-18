package com.routinelog.video.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import com.routinelog.dailyroutine.repository.DailyRoutineRepository;
import com.routinelog.user.domain.User;
import com.routinelog.video.domain.Video;
import com.routinelog.video.dto.VideoResponse;
import com.routinelog.video.dto.VideoUploadResponse;
import com.routinelog.video.metadata.VideoMetadataExtractor;
import com.routinelog.video.repository.VideoRepository;
import com.routinelog.video.storage.VideoStorage;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

	private static final long MAX_FILE_SIZE_BYTES = 100;

	@Mock
	private DailyRoutineRepository dailyRoutineRepository;

	@Mock
	private VideoRepository videoRepository;

	@Mock
	private VideoStorage videoStorage;

	@Mock
	private VideoMetadataExtractor metadataExtractor;

	private VideoService videoService;

	@BeforeEach
	void setUp() {
		videoService = new VideoService(
			dailyRoutineRepository,
			videoRepository,
			videoStorage,
			metadataExtractor,
			MAX_FILE_SIZE_BYTES
		);
	}

	@Test
	void uploadStoresVideoAndMarksDailyRoutineSuccessful() {
		DailyRoutine dailyRoutine = pendingDailyRoutine(101L, 1L);
		MockMultipartFile file = videoFile(new byte[50]);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));
		when(videoRepository.existsByDailyRoutineId(101L)).thenReturn(false);
		when(metadataExtractor.extractDuration(any(Path.class)))
			.thenReturn(new BigDecimal("12.345"));
		when(videoRepository.saveAndFlush(any(Video.class))).thenAnswer(invocation -> {
			Video video = invocation.getArgument(0);
			ReflectionTestUtils.setField(video, "id", 500L);
			return video;
		});

		VideoUploadResponse response = videoService.upload(1L, 101L, file);

		verify(videoStorage).upload(
			argThat(key -> key.matches(
				"users/1/routine-videos/2026/08/18/[0-9a-f-]+\\.mp4"
			)),
			any(Path.class),
			eq("video/mp4")
		);
		assertEquals(500L, response.video().id());
		assertEquals(new BigDecimal("12.35"), response.video().durationSeconds());
		assertEquals(50L, response.video().fileSize());
		assertEquals(RoutineStatus.SUCCESS, response.dailyRoutine().status());
	}

	@Test
	void uploadRejectsVideoLongerThanFifteenSeconds() {
		DailyRoutine dailyRoutine = pendingDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));
		when(metadataExtractor.extractDuration(any(Path.class)))
			.thenReturn(new BigDecimal("15.01"));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.upload(1L, 101L, videoFile(new byte[50]))
		);

		assertEquals(ErrorCode.VIDEO_TOO_LONG, exception.getErrorCode());
		verify(videoStorage, never()).upload(any(), any(), any());
		assertEquals(RoutineStatus.PENDING, dailyRoutine.getStatus());
	}

	@Test
	void uploadRejectsOversizedFileBeforeMetadataExtraction() {
		DailyRoutine dailyRoutine = pendingDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.upload(1L, 101L, videoFile(new byte[101]))
		);

		assertEquals(ErrorCode.VIDEO_TOO_LARGE, exception.getErrorCode());
		verify(metadataExtractor, never()).extractDuration(any());
	}

	@Test
	void uploadRejectsUnsupportedContentType() {
		DailyRoutine dailyRoutine = pendingDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"proof.webm",
			"video/webm",
			new byte[50]
		);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.upload(1L, 101L, file)
		);

		assertEquals(ErrorCode.INVALID_VIDEO_FILE, exception.getErrorCode());
		verify(metadataExtractor, never()).extractDuration(any());
	}

	@Test
	void uploadRejectsDailyRoutineOwnedByAnotherUser() {
		DailyRoutine dailyRoutine = pendingDailyRoutine(101L, 2L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.upload(1L, 101L, videoFile(new byte[50]))
		);

		assertEquals(ErrorCode.DAILY_ROUTINE_ACCESS_DENIED, exception.getErrorCode());
		verify(videoRepository, never()).existsByDailyRoutineId(any());
	}

	@Test
	void uploadRejectsExistingVideo() {
		DailyRoutine dailyRoutine = pendingDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));
		when(videoRepository.existsByDailyRoutineId(101L)).thenReturn(true);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.upload(1L, 101L, videoFile(new byte[50]))
		);

		assertEquals(ErrorCode.VIDEO_ALREADY_EXISTS, exception.getErrorCode());
	}

	@Test
	void uploadFailureLeavesDailyRoutinePending() {
		DailyRoutine dailyRoutine = pendingDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));
		when(metadataExtractor.extractDuration(any(Path.class)))
			.thenReturn(new BigDecimal("10.00"));
		org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.S3_UPLOAD_FAILED))
			.when(videoStorage).upload(any(), any(), any());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.upload(1L, 101L, videoFile(new byte[50]))
		);

		assertEquals(ErrorCode.S3_UPLOAD_FAILED, exception.getErrorCode());
		assertEquals(RoutineStatus.PENDING, dailyRoutine.getStatus());
		verify(videoRepository, never()).saveAndFlush(any());
	}

	@Test
	void findReturnsOwnedVideoWithPresignedPlaybackUrl() {
		Video video = video(500L, 101L, 1L);
		when(videoRepository.findById(500L)).thenReturn(Optional.of(video));
		when(videoStorage.createPlaybackUrl("users/1/routine-videos/proof.mp4"))
			.thenReturn("https://example.test/presigned");

		VideoResponse response = videoService.find(1L, 500L);

		assertEquals(500L, response.id());
		assertEquals(101L, response.dailyRoutineId());
		assertEquals("https://example.test/presigned", response.playbackUrl());
	}

	@Test
	void findRejectsVideoOwnedByAnotherUser() {
		Video video = video(500L, 101L, 2L);
		when(videoRepository.findById(500L)).thenReturn(Optional.of(video));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.find(1L, 500L)
		);

		assertEquals(ErrorCode.VIDEO_ACCESS_DENIED, exception.getErrorCode());
		verify(videoStorage, never()).createPlaybackUrl(any());
	}

	@Test
	void findRejectsMissingVideo() {
		when(videoRepository.findById(500L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> videoService.find(1L, 500L)
		);

		assertEquals(ErrorCode.VIDEO_NOT_FOUND, exception.getErrorCode());
	}

	private Video video(Long videoId, Long dailyRoutineId, Long userId) {
		DailyRoutine dailyRoutine = pendingDailyRoutine(dailyRoutineId, userId);
		Video video = new Video(
			dailyRoutine,
			"users/1/routine-videos/proof.mp4",
			"proof.mp4",
			"video/mp4",
			new BigDecimal("12.40"),
			4819231L
		);
		ReflectionTestUtils.setField(video, "id", videoId);
		return video;
	}

	private DailyRoutine pendingDailyRoutine(Long dailyRoutineId, Long userId) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		DailyRoutine dailyRoutine = new DailyRoutine(
			user,
			null,
			LocalDate.of(2026, 8, 18),
			"Exercise",
			LocalTime.of(7, 0)
		);
		ReflectionTestUtils.setField(dailyRoutine, "id", dailyRoutineId);
		return dailyRoutine;
	}

	private MockMultipartFile videoFile(byte[] content) {
		return new MockMultipartFile("file", "proof.mp4", "video/mp4", content);
	}
}
