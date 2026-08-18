package com.routinelog.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 인증 정보입니다."),
	AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "루틴을 찾을 수 없습니다."),
	ROUTINE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 루틴에 접근할 권한이 없습니다."),
	INVALID_REPEAT_DAY(HttpStatus.BAD_REQUEST, "반복 요일이 올바르지 않습니다."),
	DAILY_ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "날짜별 루틴을 찾을 수 없습니다."),
	DAILY_ROUTINE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 날짜별 루틴에 접근할 권한이 없습니다."),
	INVALID_DAILY_ROUTINE_STATUS(HttpStatus.CONFLICT, "날짜별 루틴 상태를 변경할 수 없습니다."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	ErrorCode(HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getMessage() {
		return message;
	}
}
