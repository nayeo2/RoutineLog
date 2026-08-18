package com.routinelog.vlog.domain;

import com.routinelog.common.domain.BaseEntity;
import com.routinelog.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "daily_vlogs",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_daily_vlogs_user_date", columnNames = {"user_id", "vlog_date"}),
		@UniqueConstraint(name = "uk_daily_vlogs_object_key", columnNames = "object_key")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyVlog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "vlog_date", nullable = false)
	private LocalDate vlogDate;

	@Column(name = "object_key", length = 1000)
	private String objectKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private VlogStatus status = VlogStatus.PROCESSING;

	@Column(name = "duration_seconds", precision = 8, scale = 2)
	private BigDecimal durationSeconds;

	@Column(name = "failure_message", length = 1000)
	private String failureMessage;

	public DailyVlog(User user, LocalDate vlogDate) {
		this.user = user;
		this.vlogDate = vlogDate;
	}

	public void succeed(String objectKey, BigDecimal durationSeconds) {
		this.objectKey = objectKey;
		this.durationSeconds = durationSeconds;
		this.failureMessage = null;
		this.status = VlogStatus.SUCCESS;
	}

	public void fail(String failureMessage) {
		this.objectKey = null;
		this.durationSeconds = null;
		this.failureMessage = failureMessage;
		this.status = VlogStatus.FAILED;
	}
}
