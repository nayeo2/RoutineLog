package com.routinelog.video.domain;

import com.routinelog.common.domain.BaseEntity;
import com.routinelog.dailyroutine.domain.DailyRoutine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "videos",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_videos_daily_routine_id", columnNames = "daily_routine_id"),
		@UniqueConstraint(name = "uk_videos_object_key", columnNames = "object_key")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "daily_routine_id", nullable = false)
	private DailyRoutine dailyRoutine;

	@Column(name = "object_key", nullable = false, length = 1000)
	private String objectKey;

	@Column(name = "original_filename", length = 255)
	private String originalFilename;

	@Column(name = "content_type", nullable = false, length = 100)
	private String contentType;

	@Column(name = "duration_seconds", nullable = false, precision = 6, scale = 2)
	private BigDecimal durationSeconds;

	@Column(name = "file_size", nullable = false)
	private Long fileSize;

	public Video(
		DailyRoutine dailyRoutine,
		String objectKey,
		String originalFilename,
		String contentType,
		BigDecimal durationSeconds,
		Long fileSize
	) {
		this.dailyRoutine = dailyRoutine;
		this.objectKey = objectKey;
		this.originalFilename = originalFilename;
		this.contentType = contentType;
		this.durationSeconds = durationSeconds;
		this.fileSize = fileSize;
	}
}
