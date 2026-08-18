package com.routinelog.video.dto;

import com.routinelog.video.domain.Video;
import java.math.BigDecimal;

public record VideoResponse(
	Long id,
	Long dailyRoutineId,
	BigDecimal durationSeconds,
	Long fileSize,
	String playbackUrl
) {

	public static VideoResponse of(Video video, String playbackUrl) {
		return new VideoResponse(
			video.getId(),
			video.getDailyRoutine().getId(),
			video.getDurationSeconds(),
			video.getFileSize(),
			playbackUrl
		);
	}
}
