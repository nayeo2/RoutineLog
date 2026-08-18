package com.routinelog.vlog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateDailyVlogRequest(
	@NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date
) {
}
