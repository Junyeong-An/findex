package com.sprint.mission.findex.global.exception;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String message,
    String details
) {

  public static ErrorResponse of(int status, String message, String details) {
    return new ErrorResponse(
        Instant.now(),
        status,
        message,
        details
    );
  }
}