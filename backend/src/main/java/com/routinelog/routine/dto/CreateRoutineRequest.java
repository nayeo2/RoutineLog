package com.routinelog.routine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record CreateRoutineRequest(
	@NotBlank @Size(max = 100) String title,
	@NotNull @JsonFormat(pattern = "HH:mm") LocalTime scheduledTime,
	@NotNull @Size(min = 1) List<@NotNull DayOfWeek> repeatDays
) {
}
