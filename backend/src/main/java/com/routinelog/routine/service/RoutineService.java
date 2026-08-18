package com.routinelog.routine.service;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.routine.domain.Routine;
import com.routinelog.routine.dto.CreateRoutineRequest;
import com.routinelog.routine.dto.RoutineResponse;
import com.routinelog.routine.repository.RoutineRepository;
import com.routinelog.user.domain.User;
import com.routinelog.user.repository.UserRepository;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineService {

	private final RoutineRepository routineRepository;
	private final UserRepository userRepository;

	@Transactional
	public RoutineResponse create(Long userId, CreateRoutineRequest request) {
		validateRepeatDays(request.repeatDays());

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		Routine routine = new Routine(
			user,
			request.title(),
			request.scheduledTime(),
			request.repeatDays()
		);

		return RoutineResponse.from(routineRepository.save(routine));
	}

	@Transactional(readOnly = true)
	public List<RoutineResponse> findAllActive(Long userId) {
		return routineRepository
			.findAllByUserIdAndActiveTrueOrderByScheduledTimeAscIdAsc(userId)
			.stream()
			.map(RoutineResponse::from)
			.toList();
	}

	private void validateRepeatDays(List<DayOfWeek> repeatDays) {
		if (new HashSet<>(repeatDays).size() != repeatDays.size()) {
			throw new BusinessException(ErrorCode.INVALID_REPEAT_DAY);
		}
	}
}
