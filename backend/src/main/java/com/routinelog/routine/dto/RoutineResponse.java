package com.routinelog.routine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.routinelog.routine.domain.Routine;
import com.routinelog.routine.domain.RoutineRepeatDay;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

public record RoutineResponse(
	Long id,
	String title,
	@JsonFormat(pattern = "HH:mm") LocalTime scheduledTime,
	List<DayOfWeek> repeatDays,
	boolean active
) {

	public static RoutineResponse from(Routine routine) {
		List<DayOfWeek> repeatDays = routine.getRepeatDays().stream()
			.map(RoutineRepeatDay::getDayOfWeek)
			.sorted(Comparator.comparingInt(DayOfWeek::getValue))
			.toList();

		return new RoutineResponse(
			routine.getId(),
			routine.getTitle(),
			routine.getScheduledTime(),
			repeatDays,
			routine.isActive()
		);
	}
}
