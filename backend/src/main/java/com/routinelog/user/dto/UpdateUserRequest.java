package com.routinelog.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
	@NotBlank @Size(max = 50) String name
) {
}
