package com.sprint.mission.findex.global.exception;

import static com.sprint.mission.findex.global.exception.ApiException.ERROR.*;

import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
    ERROR error = e.getError();
    log.warn("[ApiException] {} status: {}, code: {}, message: {}", error.name(), error.getHttpStatus().value(), error.getCode(), error.getMessage());
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
    log.warn("[ApiException] {} status: {}, code: {}, message: {} | {}", error.name(), error.getHttpStatus().value(), error.getCode(), error.getMessage(), details);
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
    log.error("[UnexpectedException] ", e);
    return ResponseEntity
        .status(error.getHttpStatus())
        .body(ErrorResponse.of(
            error.getHttpStatus().value(),
            error.getMessage(),
            error.getCode()
        ));
  }
}