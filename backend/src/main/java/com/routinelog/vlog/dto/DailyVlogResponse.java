package com.routinelog.vlog.dto;

import com.routinelog.vlog.domain.DailyVlog;
import com.routinelog.vlog.domain.VlogStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyVlogResponse(
	Long id,
	LocalDate vlogDate,
	VlogStatus status,
	BigDecimal durationSeconds,
	String playbackUrl
) {

	public static DailyVlogResponse of(DailyVlog dailyVlog, String playbackUrl) {
		return new DailyVlogResponse(
			dailyVlog.getId(),
			dailyVlog.getVlogDate(),
			dailyVlog.getStatus(),
			dailyVlog.getDurationSeconds(),
			playbackUrl
		);
	}
}
