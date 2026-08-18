package com.routinelog.dailyroutine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import com.routinelog.dailyroutine.dto.DailyRoutineResponse;
import com.routinelog.dailyroutine.dto.FailDailyRoutineRequest;
import com.routinelog.dailyroutine.dto.FailedDailyRoutineResponse;
import com.routinelog.dailyroutine.dto.PendingDailyRoutineResponse;
import com.routinelog.dailyroutine.repository.DailyRoutineRepository;
import com.routinelog.routine.domain.Routine;
import com.routinelog.routine.repository.RoutineRepository;
import com.routinelog.user.domain.User;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DailyRoutineServiceTest {

	@Mock
	private DailyRoutineRepository dailyRoutineRepository;

	@Mock
	private RoutineRepository routineRepository;

	private DailyRoutineService dailyRoutineService;

	@BeforeEach
	void setUp() {
		dailyRoutineService = new DailyRoutineService(dailyRoutineRepository, routineRepository);
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

		when(dailyRoutineRepository
			.findAllByUserIdAndRoutineDateOrderByScheduledTimeAscIdAsc(userId, routineDate))
			.thenReturn(
				List.of(existingDailyRoutine),
				List.of(existingDailyRoutine, returnedMissingDailyRoutine)
			);
		when(routineRepository.findAllByUserIdAndActiveTrueOrderByScheduledTimeAscIdAsc(userId))
			.thenReturn(List.of(existingRoutine, missingRoutine, otherDayRoutine));
		List<DailyRoutineResponse> responses = dailyRoutineService.findAllByDate(userId, routineDate);

		verify(dailyRoutineRepository).saveAll(argThat((Iterable<DailyRoutine> saved) -> {
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
