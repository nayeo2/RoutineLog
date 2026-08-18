package com.routinelog.routine.controller;

import com.routinelog.common.dto.ApiResponse;
import com.routinelog.routine.dto.CreateRoutineRequest;
import com.routinelog.routine.dto.RoutineResponse;
import com.routinelog.routine.dto.UpdateRoutineRequest;
import com.routinelog.routine.service.RoutineService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@PatchMapping("/{routineId}")
	public ResponseEntity<ApiResponse<RoutineResponse>> update(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long routineId,
		@Valid @RequestBody UpdateRoutineRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success(routineService.update(userId, routineId, request)));
	}

	@DeleteMapping("/{routineId}")
	public ResponseEntity<Void> delete(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long routineId
	) {
		routineService.delete(userId, routineId);
		return ResponseEntity.noContent().build();
	}
}
