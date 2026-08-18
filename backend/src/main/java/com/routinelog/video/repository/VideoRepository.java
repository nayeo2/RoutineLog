package com.routinelog.video.repository;

import com.routinelog.dailyroutine.domain.RoutineStatus;
import com.routinelog.video.domain.Video;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {

	Optional<Video> findByDailyRoutineId(Long dailyRoutineId);

	boolean existsByDailyRoutineId(Long dailyRoutineId);

	List<Video> findAllByDailyRoutineIdIn(List<Long> dailyRoutineIds);

	@Query("""
		select video
		from Video video
		join fetch video.dailyRoutine dailyRoutine
		where dailyRoutine.user.id = :userId
		  and dailyRoutine.routineDate = :routineDate
		  and dailyRoutine.status = :status
		order by dailyRoutine.scheduledTime asc, dailyRoutine.id asc
		""")
	List<Video> findAllByUserIdAndRoutineDateAndStatus(
		@Param("userId") Long userId,
		@Param("routineDate") LocalDate routineDate,
		@Param("status") RoutineStatus status
	);
}
