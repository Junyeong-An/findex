package com.sprint.mission.findex.global.exception;

import static com.sprint.mission.findex.global.exception.ApiException.ERROR.*;

import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
    ERROR error = e.getError();
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
    return ResponseEntity
        .status(error.getHttpStatus())
        .body(ErrorResponse.of(
            error.getHttpStatus().value(),
            error.getMessage(),
            error.getCode()
        ));
  }
}