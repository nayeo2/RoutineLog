package com.routinelog.dailyroutine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateDailyRoutineRequest(
	@NotNull LocalDate routineDate,
	@NotBlank @Size(max = 100) String title,
	@NotNull @JsonFormat(pattern = "HH:mm") LocalTime scheduledTime
) {
}
