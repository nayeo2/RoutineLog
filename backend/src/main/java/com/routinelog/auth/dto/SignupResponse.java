package com.routinelog.auth.dto;

import com.routinelog.user.domain.User;

public record SignupResponse(Long id, String email, String name) {

	public static SignupResponse from(User user) {
		return new SignupResponse(user.getId(), user.getEmail(), user.getName());
	}
}
