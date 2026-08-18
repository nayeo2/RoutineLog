package com.routinelog.routine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.routine.domain.Routine;
import com.routinelog.routine.dto.CreateRoutineRequest;
import com.routinelog.routine.dto.RoutineResponse;
import com.routinelog.routine.dto.UpdateRoutineRequest;
import com.routinelog.routine.repository.RoutineRepository;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

	@Mock
	private RoutineRepository routineRepository;

	@Mock
	private UserRepository userRepository;

	private RoutineService routineService;

	@BeforeEach
	void setUp() {
		routineService = new RoutineService(routineRepository, userRepository);
	}

	@Test
	void createSavesRoutineForAuthenticatedUser() {
		Long userId = 1L;
		User user = new User("user@example.com", "encoded-password", "User");
		CreateRoutineRequest request = new CreateRoutineRequest(
			"Morning exercise",
			LocalTime.of(7, 0),
			List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
		);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(routineRepository.save(any(Routine.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RoutineResponse response = routineService.create(userId, request);

		ArgumentCaptor<Routine> routineCaptor = ArgumentCaptor.forClass(Routine.class);
		verify(routineRepository).save(routineCaptor.capture());
		assertEquals(user, routineCaptor.getValue().getUser());
		assertEquals(request.title(), response.title());
		assertEquals(request.scheduledTime(), response.scheduledTime());
		assertEquals(request.repeatDays(), response.repeatDays());
	}

	@Test
	void createRejectsDuplicateRepeatDay() {
		CreateRoutineRequest request = new CreateRoutineRequest(
			"Morning exercise",
			LocalTime.of(7, 0),
			List.of(DayOfWeek.MONDAY, DayOfWeek.MONDAY)
		);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> routineService.create(1L, request)
		);

		assertEquals(ErrorCode.INVALID_REPEAT_DAY, exception.getErrorCode());
		verify(userRepository, never()).findById(any());
		verify(routineRepository, never()).save(any());
	}

	@Test
	void findAllActiveUsesDocumentedOrderingQuery() {
		Long userId = 1L;
		User user = new User("user@example.com", "encoded-password", "User");
		Routine morning = new Routine(
			user,
			"Morning exercise",
			LocalTime.of(7, 0),
			List.of(DayOfWeek.MONDAY)
		);
		Routine evening = new Routine(
			user,
			"Reading",
			LocalTime.of(22, 0),
			List.of(DayOfWeek.FRIDAY)
		);
		when(routineRepository.findAllByUserIdAndActiveTrueOrderByScheduledTimeAscIdAsc(userId))
			.thenReturn(List.of(morning, evening));

		List<RoutineResponse> responses = routineService.findAllActive(userId);

		assertEquals(List.of("Morning exercise", "Reading"), responses.stream()
			.map(RoutineResponse::title)
			.toList());
	}

	@Test
	void findReturnsOwnedRoutine() {
		Routine routine = ownedRoutine(1L);
		when(routineRepository.findById(10L)).thenReturn(Optional.of(routine));

		RoutineResponse response = routineService.find(1L, 10L);

		assertEquals("Morning exercise", response.title());
		assertEquals(LocalTime.of(7, 0), response.scheduledTime());
	}

	@Test
	void findRejectsRoutineOwnedByAnotherUser() {
		Routine routine = ownedRoutine(2L);
		when(routineRepository.findById(10L)).thenReturn(Optional.of(routine));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> routineService.find(1L, 10L)
		);

		assertEquals(ErrorCode.ROUTINE_ACCESS_DENIED, exception.getErrorCode());
	}

	@Test
	void updateChangesOnlyProvidedFields() {
		Long userId = 1L;
		Routine routine = ownedRoutine(userId);
		UpdateRoutineRequest request = new UpdateRoutineRequest(
			null,
			LocalTime.of(8, 0),
			List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
		);
		when(routineRepository.findById(10L)).thenReturn(Optional.of(routine));

		RoutineResponse response = routineService.update(userId, 10L, request);

		assertEquals("Morning exercise", response.title());
		assertEquals(LocalTime.of(8, 0), response.scheduledTime());
		assertEquals(List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), response.repeatDays());
	}

	@Test
	void updateRejectsRoutineOwnedByAnotherUser() {
		Routine routine = ownedRoutine(2L);
		when(routineRepository.findById(10L)).thenReturn(Optional.of(routine));

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> routineService.update(1L, 10L, new UpdateRoutineRequest("Stretching", null, null))
		);

		assertEquals(ErrorCode.ROUTINE_ACCESS_DENIED, exception.getErrorCode());
	}

	@Test
	void updateRejectsMissingRoutine() {
		when(routineRepository.findById(10L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> routineService.update(1L, 10L, new UpdateRoutineRequest("Stretching", null, null))
		);

		assertEquals(ErrorCode.ROUTINE_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	void updateRejectsDuplicateRepeatDay() {
		Routine routine = ownedRoutine(1L);
		when(routineRepository.findById(10L)).thenReturn(Optional.of(routine));
		UpdateRoutineRequest request = new UpdateRoutineRequest(
			null,
			null,
			List.of(DayOfWeek.MONDAY, DayOfWeek.MONDAY)
		);

		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> routineService.update(1L, 10L, request)
		);

		assertEquals(ErrorCode.INVALID_REPEAT_DAY, exception.getErrorCode());
	}

	@Test
	void deleteDeactivatesOwnedRoutine() {
		Routine routine = ownedRoutine(1L);
		when(routineRepository.findById(10L)).thenReturn(Optional.of(routine));

		routineService.delete(1L, 10L);

		assertFalse(routine.isActive());
		verify(routineRepository, never()).delete(any());
	}

	private Routine ownedRoutine(Long userId) {
		User user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		return new Routine(
			user,
			"Morning exercise",
			LocalTime.of(7, 0),
			List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
		);
	}
}
