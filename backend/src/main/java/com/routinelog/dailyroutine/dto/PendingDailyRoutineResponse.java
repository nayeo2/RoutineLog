package com.routinelog.dailyroutine.dto;

import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;

public record PendingDailyRoutineResponse(
	Long id,
	RoutineStatus status,
	String failureReason
) {

	public static PendingDailyRoutineResponse from(DailyRoutine dailyRoutine) {
		return new PendingDailyRoutineResponse(
			dailyRoutine.getId(),
			dailyRoutine.getStatus(),
			dailyRoutine.getFailureReason()
		);
	}
}
