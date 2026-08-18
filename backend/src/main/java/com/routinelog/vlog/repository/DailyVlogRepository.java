package com.routinelog.vlog.repository;

import com.routinelog.vlog.domain.DailyVlog;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyVlogRepository extends JpaRepository<DailyVlog, Long> {

	Optional<DailyVlog> findByUserIdAndVlogDate(Long userId, LocalDate vlogDate);
}
