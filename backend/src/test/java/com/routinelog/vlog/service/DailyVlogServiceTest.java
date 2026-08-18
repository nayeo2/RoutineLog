package com.routinelog.vlog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.domain.DailyRoutine;
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
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class DailyVlogServiceTest {

	@Mock
	private DailyVlogRepository dailyVlogRepository;

	@Mock
	private VideoRepository videoRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private VideoStorage videoStorage;

	@Mock
	private VideoMerger videoMerger;

	private DailyVlogService dailyVlogService;

	@BeforeEach
	void setUp() {
		dailyVlogService = new DailyVlogService(
			dailyVlogRepository,
			videoRepository,
			userRepository,
			videoStorage,
			videoMerger
		);
	}

	@Test
	void createMergesSuccessfulVideosInRepositoryOrder() {
		Long userId = 1L;
		LocalDate vlogDate = LocalDate.of(2026, 8, 18);
		User user = mock(User.class);
		Video morning = video(user, vlogDate, "proofs/morning.mp4", "7.25", 7);
		Video evening = video(user, vlogDate, "proofs/evening.mp4", "12.50", 22);
		when(dailyVlogRepository.findByUserIdAndVlogDate(userId, vlogDate))
			.thenReturn(Optional.empty());
		when(videoRepository.findAllByUserIdAndRoutineDateAndStatus(
			userId, vlogDate, RoutineStatus.SUCCESS
		)).thenReturn(List.of(morning, evening));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(dailyVlogRepository.saveAndFlush(any(DailyVlog.class))).thenAnswer(invocation -> {
			DailyVlog dailyVlog = invocation.getArgument(0);
			if (dailyVlog.getId() == null) {
				ReflectionTestUtils.setField(dailyVlog, "id", 300L);
			}
			return dailyVlog;
		});
		when(videoStorage.createPlaybackUrl(any())).thenReturn("https://example.test/vlog");

		DailyVlogResponse response = dailyVlogService.create(userId, vlogDate);

		InOrder downloadOrder = inOrder(videoStorage);
		downloadOrder.verify(videoStorage).download(eq("proofs/morning.mp4"), any(Path.class));
		downloadOrder.verify(videoStorage).download(eq("proofs/evening.mp4"), any(Path.class));
		verify(videoMerger).merge(argThat(paths -> paths.size() == 2), any(Path.class));
		verify(videoStorage).upload(
			argThat(key -> key.matches(
				"users/1/daily-vlogs/2026/08/2026-08-18-[0-9a-f-]+\\.mp4"
			)),
			any(Path.class),
			eq("video/mp4")
		);
		assertEquals(VlogStatus.SUCCESS, response.status());
		assertEquals(new BigDecimal("19.75"), response.durationSeconds());
		assertEquals("https://example.test/vlog", response.playbackUrl());
	}

	@Test
	void createReturnsExistingRecordWithoutRegeneration() {
		LocalDate vlogDate = LocalDate.of(2026, 8, 18);
		DailyVlog dailyVlog = successfulVlog(vlogDate);
		when(dailyVlogRepository.findByUserIdAndVlogDate(1L, vlogDate))
			.thenReturn(Optional.of(dailyVlog));
		when(videoStorage.createPlaybackUrl("vlogs/existing.mp4"))
			.thenReturn("https://example.test/existing");

		DailyVlogResponse response = dailyVlogService.create(1L, vlogDate);

		assertEquals(300L, response.id());
		assertEquals("https://example.test/existing", response.playbackUrl());
		verifyNoInteractions(videoRepository, videoMerger);
	}

	@Test
	void createRejectsDateWithoutSuccessfulVideos() {
		LocalDate vlogDate = LocalDate.of(2026, 8, 18);
		when(dailyVlogRepository.findByUserIdAndVlogDate(1L, vlogDate))
			.thenReturn(Optional.empty());
		when(videoRepository.findAllByUserIdAndRoutineDateAndStatus(
			1L, vlogDate, RoutineStatus.SUCCESS
		)).thenReturn(List.of());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyVlogService.create(1L, vlogDate)
		);

		assertEquals(ErrorCode.NO_VIDEOS_FOR_VLOG, exception.getErrorCode());
		verify(dailyVlogRepository, never()).saveAndFlush(any());
	}

	@Test
	void createRejectsConcurrentProcessingVlogWithoutRegeneration() {
		LocalDate vlogDate = LocalDate.of(2026, 8, 18);
		User user = mock(User.class);
		Video video = video(user, vlogDate, "proofs/video.mp4", "10.00", 7);
		DailyVlog processingVlog = new DailyVlog(user, vlogDate);
		when(dailyVlogRepository.findByUserIdAndVlogDate(1L, vlogDate))
			.thenReturn(Optional.empty(), Optional.of(processingVlog));
		when(videoRepository.findAllByUserIdAndRoutineDateAndStatus(
			1L, vlogDate, RoutineStatus.SUCCESS
		)).thenReturn(List.of(video));
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(dailyVlogRepository.saveAndFlush(any(DailyVlog.class)))
			.thenThrow(DataIntegrityViolationException.class);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyVlogService.create(1L, vlogDate)
		);

		assertEquals(ErrorCode.VLOG_ALREADY_PROCESSING, exception.getErrorCode());
		verifyNoInteractions(videoMerger);
	}

	@Test
	void createMarksVlogFailedWhenMergeFails() {
		LocalDate vlogDate = LocalDate.of(2026, 8, 18);
		User user = mock(User.class);
		Video video = video(user, vlogDate, "proofs/video.mp4", "10.00", 7);
		when(dailyVlogRepository.findByUserIdAndVlogDate(1L, vlogDate))
			.thenReturn(Optional.empty());
		when(videoRepository.findAllByUserIdAndRoutineDateAndStatus(
			1L, vlogDate, RoutineStatus.SUCCESS
		)).thenReturn(List.of(video));
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		AtomicReference<DailyVlog> savedVlog = new AtomicReference<>();
		when(dailyVlogRepository.saveAndFlush(any(DailyVlog.class))).thenAnswer(invocation -> {
			DailyVlog dailyVlog = invocation.getArgument(0);
			ReflectionTestUtils.setField(dailyVlog, "id", 300L);
			savedVlog.set(dailyVlog);
			return dailyVlog;
		});
		doThrow(new BusinessException(ErrorCode.VLOG_GENERATION_FAILED))
			.when(videoMerger).merge(any(), any());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyVlogService.create(1L, vlogDate)
		);

		assertEquals(ErrorCode.VLOG_GENERATION_FAILED, exception.getErrorCode());
		assertEquals(VlogStatus.FAILED, savedVlog.get().getStatus());
		verify(videoStorage, never()).upload(any(), any(), any());
	}

	@Test
	void createKeepsSuccessfulVlogWhenPlaybackUrlCreationFails() {
		LocalDate vlogDate = LocalDate.of(2026, 8, 18);
		User user = mock(User.class);
		Video video = video(user, vlogDate, "proofs/video.mp4", "10.00", 7);
		AtomicReference<DailyVlog> savedVlog = new AtomicReference<>();
		when(dailyVlogRepository.findByUserIdAndVlogDate(1L, vlogDate))
			.thenReturn(Optional.empty());
		when(videoRepository.findAllByUserIdAndRoutineDateAndStatus(
			1L, vlogDate, RoutineStatus.SUCCESS
		)).thenReturn(List.of(video));
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(dailyVlogRepository.saveAndFlush(any(DailyVlog.class))).thenAnswer(invocation -> {
			DailyVlog dailyVlog = invocation.getArgument(0);
			ReflectionTestUtils.setField(dailyVlog, "id", 300L);
			savedVlog.set(dailyVlog);
			return dailyVlog;
		});
		when(videoStorage.createPlaybackUrl(any()))
			.thenThrow(new BusinessException(ErrorCode.S3_UPLOAD_FAILED));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyVlogService.create(1L, vlogDate)
		);

		assertEquals(ErrorCode.S3_UPLOAD_FAILED, exception.getErrorCode());
		assertEquals(VlogStatus.SUCCESS, savedVlog.get().getStatus());
		verify(videoStorage, never()).delete(any());
	}

	@Test
	void findByDateRejectsMissingVlog() {
		LocalDate vlogDate = LocalDate.of(2026, 8, 18);
		when(dailyVlogRepository.findByUserIdAndVlogDate(1L, vlogDate))
			.thenReturn(Optional.empty());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyVlogService.findByDate(1L, vlogDate)
		);

		assertEquals(ErrorCode.DAILY_VLOG_NOT_FOUND, exception.getErrorCode());
	}

	private Video video(
		User user,
		LocalDate routineDate,
		String objectKey,
		String duration,
		int hour
	) {
		DailyRoutine dailyRoutine = new DailyRoutine(
			user,
			null,
			routineDate,
			"Routine",
			LocalTime.of(hour, 0)
		);
		dailyRoutine.succeed();
		return new Video(
			dailyRoutine,
			objectKey,
			"proof.mp4",
			"video/mp4",
			new BigDecimal(duration),
			100L
		);
	}

	private DailyVlog successfulVlog(LocalDate vlogDate) {
		DailyVlog dailyVlog = new DailyVlog(mock(User.class), vlogDate);
		ReflectionTestUtils.setField(dailyVlog, "id", 300L);
		dailyVlog.succeed("vlogs/existing.mp4", new BigDecimal("20.00"));
		return dailyVlog;
	}
}
