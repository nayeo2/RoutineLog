package com.routinelog.dailyroutine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record FailedDailyRoutineResponse(
	Long id,
	LocalDate routineDate,
	String title,
	@JsonFormat(pattern = "HH:mm") LocalTime scheduledTime,
	RoutineStatus status,
	String failureReason,
	DailyRoutineResponse.VideoReference video
) {

	public static FailedDailyRoutineResponse from(DailyRoutine dailyRoutine) {
		return new FailedDailyRoutineResponse(
			dailyRoutine.getId(),
			dailyRoutine.getRoutineDate(),
			dailyRoutine.getTitle(),
			dailyRoutine.getScheduledTime(),
			dailyRoutine.getStatus(),
			dailyRoutine.getFailureReason(),
			null
		);
	}
}
