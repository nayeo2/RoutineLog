package com.routinelog.routine.domain;

import com.routinelog.common.domain.BaseEntity;
import com.routinelog.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "routines",
	indexes = @Index(name = "idx_routines_user_id", columnList = "user_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Routine extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(name = "scheduled_time", nullable = false)
	private LocalTime scheduledTime;

	@Column(nullable = false)
	private boolean active = true;

	@OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RoutineRepeatDay> repeatDays = new ArrayList<>();

	public Routine(User user, String title, LocalTime scheduledTime, List<DayOfWeek> repeatDays) {
		this.user = user;
		this.title = title;
		this.scheduledTime = scheduledTime;
		repeatDays.forEach(dayOfWeek -> this.repeatDays.add(new RoutineRepeatDay(this, dayOfWeek)));
	}

	public void update(String title, LocalTime scheduledTime, List<DayOfWeek> repeatDays) {
		if (title != null) {
			this.title = title;
		}
		if (scheduledTime != null) {
			this.scheduledTime = scheduledTime;
		}
		if (repeatDays != null) {
			this.repeatDays.removeIf(repeatDay -> !repeatDays.contains(repeatDay.getDayOfWeek()));
			repeatDays.stream()
				.filter(dayOfWeek -> this.repeatDays.stream()
					.noneMatch(repeatDay -> repeatDay.getDayOfWeek() == dayOfWeek))
				.forEach(dayOfWeek -> this.repeatDays.add(new RoutineRepeatDay(this, dayOfWeek)));
		}
	}

	public void deactivate() {
		this.active = false;
	}
}
