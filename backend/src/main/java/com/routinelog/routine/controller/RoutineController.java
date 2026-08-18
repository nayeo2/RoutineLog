package com.routinelog.routine.controller;

import com.routinelog.common.dto.ApiResponse;
import com.routinelog.routine.dto.CreateRoutineRequest;
import com.routinelog.routine.dto.RoutineResponse;
import com.routinelog.routine.service.RoutineService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routines")
@RequiredArgsConstructor
public class RoutineController {

	private final RoutineService routineService;

	@PostMapping
	public ResponseEntity<ApiResponse<RoutineResponse>> create(
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody CreateRoutineRequest request
	) {
		RoutineResponse response = routineService.create(userId, request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(response));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<RoutineResponse>>> findAll(
		@AuthenticationPrincipal Long userId
	) {
		return ResponseEntity.ok(ApiResponse.success(routineService.findAllActive(userId)));
	}
}
