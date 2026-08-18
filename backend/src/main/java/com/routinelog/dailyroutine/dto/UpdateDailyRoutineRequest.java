package com.routinelog.dailyroutine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record UpdateDailyRoutineRequest(
	@Pattern(regexp = ".*\\S.*") @Size(max = 100) String title,
	@JsonFormat(pattern = "HH:mm") LocalTime scheduledTime
) {
}
