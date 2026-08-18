package com.routinelog.video.controller;

import com.routinelog.common.dto.ApiResponse;
import com.routinelog.video.dto.VideoResponse;
import com.routinelog.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoReadController {

	private final VideoService videoService;

	@GetMapping("/{videoId}")
	public ResponseEntity<ApiResponse<VideoResponse>> find(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long videoId
	) {
		return ResponseEntity.ok(ApiResponse.success(videoService.find(userId, videoId)));
	}
}
