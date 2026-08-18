package com.routinelog.user.controller;

import com.routinelog.common.dto.ApiResponse;
import com.routinelog.user.dto.ProfileImageResponse;
import com.routinelog.user.dto.UpdateUserRequest;
import com.routinelog.user.dto.UserProfileResponse;
import com.routinelog.user.dto.UserUpdateResponse;
import com.routinelog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserProfileResponse>> findMe(
		@AuthenticationPrincipal Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(userService.findMe(userId)));
	}

	@PatchMapping("/me")
	public ResponseEntity<ApiResponse<UserUpdateResponse>> updateMe(
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody UpdateUserRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success(userService.updateMe(userId, request)));
	}

	@PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<ProfileImageResponse>> updateProfileImage(
		@AuthenticationPrincipal Long userId,
		@RequestPart(value = "file", required = false) MultipartFile file
	) {
		return ResponseEntity.ok(ApiResponse.success(
			userService.updateProfileImage(userId, file)
		));
	}
}
