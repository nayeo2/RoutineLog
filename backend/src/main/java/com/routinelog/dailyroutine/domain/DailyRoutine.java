package com.routinelog.dailyroutine.domain;

import com.routinelog.common.domain.BaseEntity;
import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.routine.domain.Routine;
import com.routinelog.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "daily_routines",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_daily_routines_routine_date",
		columnNames = {"routine_id", "routine_date"}
	),
	indexes = @Index(
		name = "idx_daily_routines_user_date",
		columnList = "user_id,routine_date"
	)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRoutine extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "routine_id")
	private Routine routine;

	@Column(name = "routine_date", nullable = false)
	private LocalDate routineDate;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(name = "scheduled_time", nullable = false)
	private LocalTime scheduledTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RoutineStatus status = RoutineStatus.PENDING;

	@Column(name = "failure_reason", length = 500)
	private String failureReason;

	public DailyRoutine(
		User user,
		Routine routine,
		LocalDate routineDate,
		String title,
		LocalTime scheduledTime
	) {
		this.user = user;
		this.routine = routine;
		this.routineDate = routineDate;
		this.title = title;
		this.scheduledTime = scheduledTime;
	}

	public void fail(String failureReason) {
		if (status != RoutineStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_DAILY_ROUTINE_STATUS);
		}
		this.status = RoutineStatus.FAILED;
		this.failureReason = failureReason;
	}

	public void restorePending() {
		if (status != RoutineStatus.FAILED) {
			throw new BusinessException(ErrorCode.INVALID_DAILY_ROUTINE_STATUS);
		}
		this.status = RoutineStatus.PENDING;
		this.failureReason = null;
	}

	public void succeed() {
		if (status != RoutineStatus.PENDING) {
			throw new BusinessException(ErrorCode.INVALID_DAILY_ROUTINE_STATUS);
		}
		this.status = RoutineStatus.SUCCESS;
		this.failureReason = null;
	}

	public void update(String title, LocalTime scheduledTime) {
		if (title != null) {
			this.title = title;
		}
		if (scheduledTime != null) {
			this.scheduledTime = scheduledTime;
		}
	}
}
