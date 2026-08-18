package com.routinelog.routine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record UpdateRoutineRequest(
	@Pattern(regexp = ".*\\S.*") @Size(max = 100) String title,
	@JsonFormat(pattern = "HH:mm") LocalTime scheduledTime,
	@Size(min = 1) List<@NotNull DayOfWeek> repeatDays
) {
}
