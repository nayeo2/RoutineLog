package com.routinelog.dailyroutine.repository;

import com.routinelog.dailyroutine.domain.DailyRoutine;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRoutineRepository extends JpaRepository<DailyRoutine, Long> {

	List<DailyRoutine> findAllByUserIdAndRoutineDateOrderByScheduledTimeAscIdAsc(
		Long userId,
		LocalDate routineDate
	);

	boolean existsByRoutineIdAndRoutineDate(Long routineId, LocalDate routineDate);
}
