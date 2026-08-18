package com.routinelog.dailyroutine.service;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.dto.DailyRoutineResponse;
import com.routinelog.dailyroutine.dto.FailDailyRoutineRequest;
import com.routinelog.dailyroutine.dto.FailedDailyRoutineResponse;
import com.routinelog.dailyroutine.dto.PendingDailyRoutineResponse;
import com.routinelog.dailyroutine.repository.DailyRoutineRepository;
import com.routinelog.routine.domain.Routine;
import com.routinelog.routine.domain.RoutineRepeatDay;
import com.routinelog.routine.repository.RoutineRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyRoutineService {

	private final DailyRoutineRepository dailyRoutineRepository;
	private final RoutineRepository routineRepository;

	@Transactional
	public List<DailyRoutineResponse> findAllByDate(Long userId, LocalDate routineDate) {
		List<DailyRoutine> existingDailyRoutines = dailyRoutineRepository
			.findAllByUserIdAndRoutineDateOrderByScheduledTimeAscIdAsc(userId, routineDate);
		Set<Long> existingRoutineIds = findExistingRoutineIds(existingDailyRoutines);

		DayOfWeek dayOfWeek = routineDate.getDayOfWeek();
		List<DailyRoutine> missingDailyRoutines = routineRepository
			.findAllByUserIdAndActiveTrueOrderByScheduledTimeAscIdAsc(userId)
			.stream()
			.filter(routine -> repeatsOn(routine, dayOfWeek))
			.filter(routine -> !existingRoutineIds.contains(routine.getId()))
			.map(routine -> createSnapshot(routine, routineDate))
			.toList();

		if (!missingDailyRoutines.isEmpty()) {
			dailyRoutineRepository.saveAll(missingDailyRoutines);
		}

		return dailyRoutineRepository
			.findAllByUserIdAndRoutineDateOrderByScheduledTimeAscIdAsc(userId, routineDate)
			.stream()
			.map(DailyRoutineResponse::from)
			.toList();
	}

	@Transactional
	public FailedDailyRoutineResponse fail(
		Long userId,
		Long dailyRoutineId,
		FailDailyRoutineRequest request
	) {
		DailyRoutine dailyRoutine = findOwnedDailyRoutine(userId, dailyRoutineId);
		dailyRoutine.fail(request.failureReason());
		return FailedDailyRoutineResponse.from(dailyRoutine);
	}

	@Transactional
	public PendingDailyRoutineResponse restorePending(Long userId, Long dailyRoutineId) {
		DailyRoutine dailyRoutine = findOwnedDailyRoutine(userId, dailyRoutineId);
		dailyRoutine.restorePending();
		return PendingDailyRoutineResponse.from(dailyRoutine);
	}

	private DailyRoutine findOwnedDailyRoutine(Long userId, Long dailyRoutineId) {
		DailyRoutine dailyRoutine = dailyRoutineRepository.findById(dailyRoutineId)
			.orElseThrow(() -> new BusinessException(ErrorCode.DAILY_ROUTINE_NOT_FOUND));
		if (!Objects.equals(dailyRoutine.getUser().getId(), userId)) {
			throw new BusinessException(ErrorCode.DAILY_ROUTINE_ACCESS_DENIED);
		}
		return dailyRoutine;
	}

	private Set<Long> findExistingRoutineIds(List<DailyRoutine> dailyRoutines) {
		Set<Long> routineIds = new HashSet<>();
		dailyRoutines.stream()
			.map(DailyRoutine::getRoutine)
			.filter(Objects::nonNull)
			.map(Routine::getId)
			.forEach(routineIds::add);
		return routineIds;
	}

	private boolean repeatsOn(Routine routine, DayOfWeek dayOfWeek) {
		return routine.getRepeatDays().stream()
			.map(RoutineRepeatDay::getDayOfWeek)
			.anyMatch(dayOfWeek::equals);
	}

	private DailyRoutine createSnapshot(Routine routine, LocalDate routineDate) {
		return new DailyRoutine(
			routine.getUser(),
			routine,
			routineDate,
			routine.getTitle(),
			routine.getScheduledTime()
		);
	}
}
