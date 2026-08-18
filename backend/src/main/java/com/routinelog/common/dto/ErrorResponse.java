package com.routinelog.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.routinelog.common.exception.ErrorCode;
import java.util.Map;

public record ErrorResponse(
	String code,
	String message,
	@JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> fields
) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), null);
	}

	public static ErrorResponse of(ErrorCode errorCode, Map<String, String> fields) {
		return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fields);
	}
}
