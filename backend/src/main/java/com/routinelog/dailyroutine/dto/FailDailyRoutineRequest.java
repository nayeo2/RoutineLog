package com.routinelog.dailyroutine.dto;

import jakarta.validation.constraints.Size;

public record FailDailyRoutineRequest(
	@Size(max = 500) String failureReason
) {
}
