package com.planwith.planwith_fo_story.exception;

import java.time.Instant;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.exception.ScheduleNotOwnedException;
import com.planwith.planwith_fo_story.domain.exception.MemberAuthenticationRequiredException;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.dto.ApiErrorResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
	}

	@ExceptionHandler(MemberAuthenticationRequiredException.class)
	public ResponseEntity<ApiErrorResponse> handleMemberAuthenticationRequired(
			MemberAuthenticationRequiredException exception
	) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "MEMBER_AUTHENTICATION_REQUIRED", exception.getMessage());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException exception) {
		if ("X-Member-UUID".equals(exception.getHeaderName())) {
			return createErrorResponse(
					HttpStatus.UNAUTHORIZED,
					"MEMBER_AUTHENTICATION_REQUIRED",
					"로그인한 회원만 스토리를 작성할 수 있습니다."
			);
		}
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
	}

	@ExceptionHandler(StoryNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleStoryNotFound(StoryNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(StoryAccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleStoryAccessDenied(StoryAccessDeniedException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, "STORY_ACCESS_DENIED", exception.getMessage());
	}

	@ExceptionHandler(ScheduleNotOwnedException.class)
	public ResponseEntity<ApiErrorResponse> handleScheduleNotOwned(ScheduleNotOwnedException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "SCHEDULE_NOT_OWNED", exception.getMessage());
	}

	@ExceptionHandler(InvalidStoryStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidStoryState(InvalidStoryStateException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_STORY_STATE", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.orElse("요청값이 올바르지 않습니다.");

		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		String message = exception.getConstraintViolations()
				.stream()
				.findFirst()
				.map(violation -> violation.getMessage())
				.orElse("요청값이 올바르지 않습니다.");
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	private ResponseEntity<ApiErrorResponse> createErrorResponse(
			HttpStatus status,
			String code,
			String message
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				code,
				message
		);
		return ResponseEntity.status(status).body(response);
	}
}
