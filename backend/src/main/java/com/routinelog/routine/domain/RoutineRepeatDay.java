package com.routinelog.routine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.DayOfWeek;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "routine_repeat_days",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_routine_repeat_days_routine_day",
		columnNames = {"routine_id", "day_of_week"}
	)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineRepeatDay {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "routine_id", nullable = false)
	private Routine routine;

	@Enumerated(EnumType.STRING)
	@Column(name = "day_of_week", nullable = false, length = 20)
	private DayOfWeek dayOfWeek;

	RoutineRepeatDay(Routine routine, DayOfWeek dayOfWeek) {
		this.routine = routine;
		this.dayOfWeek = dayOfWeek;
	}
}
