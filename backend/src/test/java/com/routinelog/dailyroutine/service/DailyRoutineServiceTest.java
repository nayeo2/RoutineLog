package com.routinelog.dailyroutine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import com.routinelog.dailyroutine.dto.CreateDailyRoutineRequest;
import com.routinelog.dailyroutine.dto.DailyRoutineResponse;
import com.routinelog.dailyroutine.dto.FailDailyRoutineRequest;
import com.routinelog.dailyroutine.dto.FailedDailyRoutineResponse;
import com.routinelog.dailyroutine.dto.PendingDailyRoutineResponse;
import com.routinelog.dailyroutine.dto.UpdateDailyRoutineRequest;
import com.routinelog.dailyroutine.repository.DailyRoutineRepository;
import com.routinelog.routine.domain.Routine;
import com.routinelog.routine.repository.RoutineRepository;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import com.routinelog.video.domain.Video;
import com.routinelog.video.repository.VideoRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DailyRoutineServiceTest {

	@Mock
	private DailyRoutineRepository dailyRoutineRepository;

	@Mock
	private RoutineRepository routineRepository;

	@Mock
	private VideoRepository videoRepository;

	@Mock
	private UserRepository userRepository;

	private DailyRoutineService dailyRoutineService;

	@BeforeEach
	void setUp() {
		dailyRoutineService = new DailyRoutineService(
			dailyRoutineRepository,
			routineRepository,
			videoRepository,
			userRepository
		);
	}

	@Test
	void createSavesOneOffPendingDailyRoutine() {
		User user = mock(User.class);
		CreateDailyRoutineRequest request = new CreateDailyRoutineRequest(
			LocalDate.of(2026, 8, 18),
			"Visit bank",
			LocalTime.of(14, 0)
		);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(dailyRoutineRepository.save(argThat(dailyRoutine ->
			dailyRoutine.getRoutine() == null
		))).thenAnswer(invocation -> invocation.getArgument(0));

		DailyRoutineResponse response = dailyRoutineService.create(1L, request);

		assertNull(response.routineId());
		assertEquals(RoutineStatus.PENDING, response.status());
		assertEquals("Visit bank", response.title());
	}

	@Test
	@SuppressWarnings("unchecked")
	void findAllByDateCreatesOnlyMatchingMissingRoutineSnapshots() {
		Long userId = 1L;
		LocalDate routineDate = LocalDate.of(2026, 8, 18);
		User user = mock(User.class);
		Routine existingRoutine = routine(10L, user, "Exercise", 7, DayOfWeek.TUESDAY);
		Routine missingRoutine = routine(11L, user, "Reading", 22, DayOfWeek.TUESDAY);
		Routine otherDayRoutine = routine(12L, user, "Walk", 18, DayOfWeek.MONDAY);
		DailyRoutine existingDailyRoutine = dailyRoutine(101L, user, existingRoutine, routineDate);
		DailyRoutine returnedMissingDailyRoutine = dailyRoutine(102L, user, missingRoutine, routineDate);
		Video video = new Video(
			existingDailyRoutine,
			"users/1/videos/proof.mp4",
			"proof.mp4",
			"video/mp4",
			new BigDecimal("10.00"),
			100L
		);
		ReflectionTestUtils.setField(video, "id", 500L);

		when(dailyRoutineRepository
			.findAllByUserIdAndRoutineDateOrderByScheduledTimeAscIdAsc(userId, routineDate))
			.thenReturn(
				List.of(existingDailyRoutine),
				List.of(existingDailyRoutine, returnedMissingDailyRoutine)
			);
		when(routineRepository.findAllByUserIdAndActiveTrueOrderByScheduledTimeAscIdAsc(userId))
			.thenReturn(List.of(existingRoutine, missingRoutine, otherDayRoutine));
		when(videoRepository.findAllByDailyRoutineIdIn(List.of(101L, 102L)))
			.thenReturn(List.of(video));
		List<DailyRoutineResponse> responses = dailyRoutineService.findAllByDate(userId, routineDate);

		verify(dailyRoutineRepository).saveAllAndFlush(argThat((Iterable<DailyRoutine> saved) -> {
			List<DailyRoutine> dailyRoutines = new ArrayList<>();
			saved.forEach(dailyRoutines::add);
			return dailyRoutines.size() == 1
				&& dailyRoutines.getFirst().getRoutine().getId().equals(11L)
				&& dailyRoutines.getFirst().getTitle().equals("Reading")
				&& dailyRoutines.getFirst().getRoutineDate().equals(routineDate);
		}));
		assertEquals(List.of(10L, 11L), responses.stream()
			.map(DailyRoutineResponse::routineId)
			.toList());
		assertEquals(List.of("Exercise", "Reading"), responses.stream()
			.map(DailyRoutineResponse::title)
			.toList());
		assertEquals(500L, responses.getFirst().video().id());
		assertNull(responses.get(1).video());
	}

	@Test
	void findAllByDateMapsConcurrentSnapshotCreationConflict() {
		Long userId = 1L;
		LocalDate routineDate = LocalDate.of(2026, 8, 18);
		User user = mock(User.class);
		Routine routine = routine(10L, user, "Exercise", 7, DayOfWeek.TUESDAY);
		when(dailyRoutineRepository
			.findAllByUserIdAndRoutineDateOrderByScheduledTimeAscIdAsc(userId, routineDate))
			.thenReturn(List.of());
		when(routineRepository.findAllByUserIdAndActiveTrueOrderByScheduledTimeAscIdAsc(userId))
			.thenReturn(List.of(routine));
		when(dailyRoutineRepository.saveAllAndFlush(argThat((Iterable<DailyRoutine> saved) -> true)))
			.thenThrow(DataIntegrityViolationException.class);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyRoutineService.findAllByDate(userId, routineDate)
		);

		assertEquals(ErrorCode.DAILY_ROUTINE_ALREADY_EXISTS, exception.getErrorCode());
	}

	@Test
	void failChangesOwnedPendingRoutineToFailed() {
		DailyRoutine dailyRoutine = ownedDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		FailedDailyRoutineResponse response = dailyRoutineService.fail(
			1L,
			101L,
			new FailDailyRoutineRequest("Worked late")
		);

		assertEquals(RoutineStatus.FAILED, response.status());
		assertEquals("Worked late", response.failureReason());
	}

	@Test
	void updateChangesOnlyDailyRoutineSnapshot() {
		DailyRoutine dailyRoutine = ownedDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));
		when(videoRepository.findByDailyRoutineId(101L)).thenReturn(Optional.empty());

		DailyRoutineResponse response = dailyRoutineService.update(
			1L,
			101L,
			new UpdateDailyRoutineRequest("Visit bank and post office", LocalTime.of(15, 0))
		);

		assertEquals("Visit bank and post office", response.title());
		assertEquals(LocalTime.of(15, 0), response.scheduledTime());
	}

	@Test
	void deleteRemovesOwnedPendingDailyRoutine() {
		DailyRoutine dailyRoutine = ownedDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		dailyRoutineService.delete(1L, 101L);

		verify(dailyRoutineRepository).delete(dailyRoutine);
	}

	@Test
	void deleteRejectsSuccessfulDailyRoutine() {
		DailyRoutine dailyRoutine = ownedDailyRoutine(101L, 1L);
		dailyRoutine.succeed();
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyRoutineService.delete(1L, 101L)
		);

		assertEquals(ErrorCode.INVALID_DAILY_ROUTINE_STATUS, exception.getErrorCode());
		verify(dailyRoutineRepository, never()).delete(dailyRoutine);
	}

	@Test
	void restorePendingClearsFailureReason() {
		DailyRoutine dailyRoutine = ownedDailyRoutine(101L, 1L);
		dailyRoutine.fail("Worked late");
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		PendingDailyRoutineResponse response = dailyRoutineService.restorePending(1L, 101L);

		assertEquals(RoutineStatus.PENDING, response.status());
		assertNull(response.failureReason());
	}

	@Test
	void restorePendingRejectsPendingRoutine() {
		DailyRoutine dailyRoutine = ownedDailyRoutine(101L, 1L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyRoutineService.restorePending(1L, 101L)
		);

		assertEquals(ErrorCode.INVALID_DAILY_ROUTINE_STATUS, exception.getErrorCode());
	}

	@Test
	void failRejectsRoutineOwnedByAnotherUser() {
		DailyRoutine dailyRoutine = ownedDailyRoutine(101L, 2L);
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.of(dailyRoutine));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyRoutineService.fail(1L, 101L, new FailDailyRoutineRequest(null))
		);

		assertEquals(ErrorCode.DAILY_ROUTINE_ACCESS_DENIED, exception.getErrorCode());
	}

	@Test
	void failRejectsMissingRoutine() {
		when(dailyRoutineRepository.findById(101L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> dailyRoutineService.fail(1L, 101L, new FailDailyRoutineRequest(null))
		);

		assertEquals(ErrorCode.DAILY_ROUTINE_NOT_FOUND, exception.getErrorCode());
	}

	private Routine routine(Long id, User user, String title, int hour, DayOfWeek repeatDay) {
		Routine routine = new Routine(user, title, LocalTime.of(hour, 0), List.of(repeatDay));
		ReflectionTestUtils.setField(routine, "id", id);
		return routine;
	}

	private DailyRoutine dailyRoutine(
		Long id,
		User user,
		Routine routine,
		LocalDate routineDate
	) {
		DailyRoutine dailyRoutine = new DailyRoutine(
			user,
			routine,
			routineDate,
			routine.getTitle(),
			routine.getScheduledTime()
		);
		ReflectionTestUtils.setField(dailyRoutine, "id", id);
		return dailyRoutine;
	}

	private DailyRoutine ownedDailyRoutine(Long id, Long userId) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		DailyRoutine dailyRoutine = new DailyRoutine(
			user,
			null,
			LocalDate.of(2026, 8, 18),
			"Exercise",
			LocalTime.of(7, 0)
		);
		ReflectionTestUtils.setField(dailyRoutine, "id", id);
		return dailyRoutine;
	}
}
