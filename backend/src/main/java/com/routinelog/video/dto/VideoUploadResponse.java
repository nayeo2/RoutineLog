package com.routinelog.video.dto;

import com.routinelog.dailyroutine.domain.DailyRoutine;
import com.routinelog.dailyroutine.domain.RoutineStatus;
import com.routinelog.video.domain.Video;
import java.math.BigDecimal;

public record VideoUploadResponse(
	VideoData video,
	DailyRoutineData dailyRoutine
) {

	public static VideoUploadResponse of(Video video, DailyRoutine dailyRoutine) {
		return new VideoUploadResponse(
			new VideoData(
				video.getId(),
				dailyRoutine.getId(),
				video.getDurationSeconds(),
				video.getFileSize()
			),
			new DailyRoutineData(
				dailyRoutine.getId(),
				dailyRoutine.getStatus(),
				dailyRoutine.getFailureReason()
			)
		);
	}

	public record VideoData(
		Long id,
		Long dailyRoutineId,
		BigDecimal durationSeconds,
		Long fileSize
	) {
	}

	public record DailyRoutineData(
		Long id,
		RoutineStatus status,
		String failureReason
	) {
	}
}
