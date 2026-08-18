package com.routinelog.dailyroutine.controller;

import com.routinelog.common.dto.ApiResponse;
import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.dailyroutine.dto.DailyRoutineResponse;
import com.routinelog.dailyroutine.dto.FailDailyRoutineRequest;
import com.routinelog.dailyroutine.dto.FailedDailyRoutineResponse;
import com.routinelog.dailyroutine.dto.PendingDailyRoutineResponse;
import com.routinelog.dailyroutine.service.DailyRoutineService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/daily-routines")
@RequiredArgsConstructor
public class DailyRoutineController {

	private final DailyRoutineService dailyRoutineService;

	@GetMapping
	public ResponseEntity<ApiResponse<List<DailyRoutineResponse>>> findAllByDate(
		@AuthenticationPrincipal Long userId,
		@RequestParam(required = false) String date
	) {
		LocalDate routineDate = parseDate(date);
		return ResponseEntity.ok(ApiResponse.success(
			dailyRoutineService.findAllByDate(userId, routineDate)
		));
	}

	@PatchMapping("/{dailyRoutineId}/failed")
	public ResponseEntity<ApiResponse<FailedDailyRoutineResponse>> fail(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long dailyRoutineId,
		@Valid @RequestBody FailDailyRoutineRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success(
			dailyRoutineService.fail(userId, dailyRoutineId, request)
		));
	}

	@PatchMapping("/{dailyRoutineId}/pending")
	public ResponseEntity<ApiResponse<PendingDailyRoutineResponse>> restorePending(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long dailyRoutineId
	) {
		return ResponseEntity.ok(ApiResponse.success(
			dailyRoutineService.restorePending(userId, dailyRoutineId)
		));
	}

	private LocalDate parseDate(String date) {
		if (date == null) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		try {
			return LocalDate.parse(date);
		} catch (DateTimeParseException exception) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}
}
