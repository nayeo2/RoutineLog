package com.routinelog.dailyroutine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record DailyRoutineResponse(
	Long id,
	Long routineId,
	LocalDate routineDate,
	String title,
	@JsonFormat(pattern = "HH:mm") LocalTime scheduledTime,
	RoutineStatus status,
	String failureReason,
	VideoReference video
) {

	public static DailyRoutineResponse from(DailyRoutine dailyRoutine) {
		Long routineId = dailyRoutine.getRoutine() == null
			? null
			: dailyRoutine.getRoutine().getId();

		return new DailyRoutineResponse(
			dailyRoutine.getId(),
			routineId,
			dailyRoutine.getRoutineDate(),
			dailyRoutine.getTitle(),
			dailyRoutine.getScheduledTime(),
			dailyRoutine.getStatus(),
			dailyRoutine.getFailureReason(),
			null
		);
	}

	public record VideoReference(Long id) {
	}
}
