package com.routinelog.dailyroutine.service;

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
import com.routinelog.routine.domain.RoutineRepeatDay;
import com.routinelog.routine.repository.RoutineRepository;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import com.routinelog.video.domain.Video;
import com.routinelog.video.repository.VideoRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyRoutineService {

	private final DailyRoutineRepository dailyRoutineRepository;
	private final RoutineRepository routineRepository;
	private final VideoRepository videoRepository;
	private final UserRepository userRepository;

	@Transactional
	public DailyRoutineResponse create(Long userId, CreateDailyRoutineRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		DailyRoutine dailyRoutine = new DailyRoutine(
			user,
			null,
			request.routineDate(),
			request.title(),
			request.scheduledTime()
		);
		return DailyRoutineResponse.from(dailyRoutineRepository.save(dailyRoutine));
	}

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
			try {
				dailyRoutineRepository.saveAllAndFlush(missingDailyRoutines);
			} catch (DataIntegrityViolationException exception) {
				throw new BusinessException(ErrorCode.DAILY_ROUTINE_ALREADY_EXISTS);
			}
		}

		List<DailyRoutine> dailyRoutines = dailyRoutineRepository
			.findAllByUserIdAndRoutineDateOrderByScheduledTimeAscIdAsc(userId, routineDate);
		Map<Long, Video> videosByDailyRoutineId = findVideosByDailyRoutineId(dailyRoutines);

		return dailyRoutines
			.stream()
			.map(dailyRoutine -> {
				Video video = videosByDailyRoutineId.get(dailyRoutine.getId());
				return DailyRoutineResponse.from(
					dailyRoutine,
					video == null ? null : video.getId()
				);
			})
			.toList();
	}

	@Transactional
	public DailyRoutineResponse update(
		Long userId,
		Long dailyRoutineId,
		UpdateDailyRoutineRequest request
	) {
		DailyRoutine dailyRoutine = findOwnedDailyRoutine(userId, dailyRoutineId);
		dailyRoutine.update(request.title(), request.scheduledTime());
		Long videoId = videoRepository.findByDailyRoutineId(dailyRoutineId)
			.map(Video::getId)
			.orElse(null);
		return DailyRoutineResponse.from(dailyRoutine, videoId);
	}

	@Transactional
	public void delete(Long userId, Long dailyRoutineId) {
		DailyRoutine dailyRoutine = findOwnedDailyRoutine(userId, dailyRoutineId);
		if (dailyRoutine.getStatus() == RoutineStatus.SUCCESS) {
			throw new BusinessException(ErrorCode.INVALID_DAILY_ROUTINE_STATUS);
		}
		dailyRoutineRepository.delete(dailyRoutine);
	}

	private Map<Long, Video> findVideosByDailyRoutineId(List<DailyRoutine> dailyRoutines) {
		List<Long> dailyRoutineIds = dailyRoutines.stream()
			.map(DailyRoutine::getId)
			.filter(Objects::nonNull)
			.toList();
		if (dailyRoutineIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return videoRepository.findAllByDailyRoutineIdIn(dailyRoutineIds).stream()
			.collect(Collectors.toMap(
				video -> video.getDailyRoutine().getId(),
				Function.identity()
			));
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
