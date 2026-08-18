package com.routinelog.user.dto;

import com.routinelog.user.domain.User;

public record UserUpdateResponse(
	Long id,
	String email,
	String name,
	String profileImageUrl
) {

	public static UserUpdateResponse of(User user, String profileImageUrl) {
		return new UserUpdateResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			profileImageUrl
		);
	}
}
