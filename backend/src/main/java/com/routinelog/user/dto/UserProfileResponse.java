package com.routinelog.user.dto;

import com.routinelog.user.domain.User;
import java.time.LocalDateTime;

public record UserProfileResponse(
	Long id,
	String email,
	String name,
	String profileImageUrl,
	LocalDateTime createdAt
) {

	public static UserProfileResponse of(User user, String profileImageUrl) {
		return new UserProfileResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			profileImageUrl,
			user.getCreatedAt()
		);
	}
}
