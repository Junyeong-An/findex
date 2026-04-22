package com.sprint.mission.findex.global.exception;

import static com.sprint.mission.findex.global.exception.ApiException.ERROR.COMMON_INVALID_REQUEST;
import static com.sprint.mission.findex.global.exception.ApiException.ERROR.COMMON_UNEXPECTED_ERROR;

import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
    ERROR error = e.getError();
    log.warn("[ApiException] {} status: {}, code: {}, message: {}", error.name(),
        error.getHttpStatus().value(), error.getCode(), error.getMessage());
    return ResponseEntity
        .status(error.getHttpStatus())
        .body(ErrorResponse.of(
            error.getHttpStatus().value(),
            error.getMessage(),
            error.getCode()
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {
    ERROR error = COMMON_INVALID_REQUEST;
    String details = e.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.warn("[ApiException] {} status: {}, code: {}, message: {} | {}", error.name(),
        error.getHttpStatus().value(), error.getCode(), error.getMessage(), details);
    return ResponseEntity
        .status(error.getHttpStatus())
        .body(ErrorResponse.of(
            error.getHttpStatus().value(),
            error.getMessage(),
            error.getCode() + " | " + details
        ));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    ERROR error = COMMON_INVALID_REQUEST;
    String details = String.format("%s: %s 타입이어야 합니다",
        e.getName(),
        e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown"
    );
    log.warn("[ApiException] {} status: {}, code: {}, message: {} | {}", error.name(),
        error.getHttpStatus().value(), error.getCode(), error.getMessage(), details);
    return ResponseEntity
        .status(error.getHttpStatus())
        .body(ErrorResponse.of(
            error.getHttpStatus().value(),
            error.getMessage(),
            error.getCode() + " | " + details
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    ERROR error = COMMON_UNEXPECTED_ERROR;
    log.error("[UnexpectedException] cause: {}, message: {}", e.getClass().getSimpleName(),
        e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
    return ResponseEntity
        .status(error.getHttpStatus())
        .body(ErrorResponse.of(
            error.getHttpStatus().value(),
            error.getMessage(),
            error.getCode()
        ));
  }
}