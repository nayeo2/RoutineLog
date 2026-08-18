package com.routinelog.routine.repository;

import com.routinelog.routine.domain.Routine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

	List<Routine> findAllByUserIdAndActiveTrue(Long userId);
}
