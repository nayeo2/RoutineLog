package com.routinelog.auth.controller;

import com.routinelog.auth.dto.SignupRequest;
import com.routinelog.auth.dto.SignupResponse;
import com.routinelog.auth.service.AuthService;
import com.routinelog.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<SignupResponse>> signup(
		@Valid @RequestBody SignupRequest request
	) {
		SignupResponse response = authService.signup(request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(response));
	}
}
