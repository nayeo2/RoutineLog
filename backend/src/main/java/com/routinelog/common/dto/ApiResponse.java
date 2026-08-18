package com.routinelog.common.dto;

import com.routinelog.common.exception.ErrorCode;
import java.util.Map;

public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static ApiResponse<Void> failure(ErrorCode errorCode) {
		return new ApiResponse<>(false, null, ErrorResponse.of(errorCode));
	}

	public static ApiResponse<Void> failure(ErrorCode errorCode, Map<String, String> fields) {
		return new ApiResponse<>(false, null, ErrorResponse.of(errorCode, fields));
	}
}
