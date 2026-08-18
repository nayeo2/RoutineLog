package com.routinelog.vlog.controller;

import com.routinelog.common.dto.ApiResponse;
import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import com.routinelog.vlog.dto.CreateDailyVlogRequest;
import com.routinelog.vlog.dto.DailyVlogResponse;
import com.routinelog.vlog.service.DailyVlogService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vlogs")
@RequiredArgsConstructor
public class DailyVlogController {

	private final DailyVlogService dailyVlogService;

	@PostMapping
	public ResponseEntity<ApiResponse<DailyVlogResponse>> create(
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody CreateDailyVlogRequest request
	) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(dailyVlogService.create(userId, request.date())));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<DailyVlogResponse>> findByDate(
		@AuthenticationPrincipal Long userId,
		@RequestParam(required = false) String date
	) {
		return ResponseEntity.ok(ApiResponse.success(
			dailyVlogService.findByDate(userId, parseDate(date))
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
