package com.routinelog.video.controller;

import com.routinelog.common.dto.ApiResponse;
import com.routinelog.video.dto.VideoUploadResponse;
import com.routinelog.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/daily-routines/{dailyRoutineId}/videos")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<VideoUploadResponse>> upload(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long dailyRoutineId,
		@RequestPart(value = "file", required = false) MultipartFile file
	) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(videoService.upload(userId, dailyRoutineId, file)));
	}
}
