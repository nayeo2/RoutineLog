package com.routinelog.common.exception;

import com.routinelog.common.dto.ApiResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorCode));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
		MethodArgumentNotValidException exception
	) {
		Map<String, String> fields = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
			fields.putIfAbsent(error.getField(), error.getDefaultMessage())
		);

		ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorCode, fields));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable() {
		ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorCode));
	}

	@ExceptionHandler({
		MethodArgumentTypeMismatchException.class,
		MissingServletRequestParameterException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleRequestBindingException() {
		ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorCode));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed() {
		ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorCode));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded() {
		ErrorCode errorCode = ErrorCode.VIDEO_TOO_LARGE;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorCode));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		log.error("Unhandled exception", exception);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.failure(errorCode));
	}
}
