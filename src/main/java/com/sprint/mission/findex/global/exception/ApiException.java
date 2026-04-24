package com.sprint.mission.findex.global.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

  private final ERROR error;

  public enum ERROR {

    // TODO: This is example. Please write for each domain.
    // IndexInfo
    INDEX_INFO_NOT_FOUND("INDEX_001", "지수 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INDEX_INFO_DUPLICATED("INDEX_002", "이미 존재하는 지수 정보입니다.", HttpStatus.CONFLICT),

    // IndexData
    INDEX_DATA_NOT_FOUND("INDEX_DATA_001", "지수 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INDEX_DATA_DUPLICATED("INDEX_DATA_002", "이미 존재하는 지수 데이터입니다.", HttpStatus.CONFLICT),
    INDEX_DATA_CSV_EXPORT_FAILED("INDEX_DATA_003", "CSV Export에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // SyncJob
    SYNC_JOB_NOT_FOUND("SYNC_001", "연동 작업을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SYNC_JOB_OPEN_API_ERROR("SYNC_002", "Open API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),

    // AutoSyncConfig
    AUTO_SYNC_CONFIG_NOT_FOUND("AUTO_SYNC_001", "자동 연동 설정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_SORT_FIELD("AUTO_SYNC_002", "유효하지 않은 정렬 필드입니다.", HttpStatus.BAD_REQUEST),

    // Common
    COMMON_INVALID_REQUEST("COMMON_001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    COMMON_UNEXPECTED_ERROR("COMMON_002", "예기치 않은 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);



    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ERROR(String code, String message, HttpStatus httpStatus) {
      this.code = code;
      this.message = message;
      this.httpStatus = httpStatus;
    }

    public String getCode() {
      return code;
    }

    public String getMessage() {
      return message;
    }

    public HttpStatus getHttpStatus() {
      return httpStatus;
    }
  }

  public ApiException(ERROR error) {
    super(error.getMessage());
    this.error = error;
  }

  public ApiException(ERROR error, Throwable cause) {
    super(error.getMessage(), cause);
    this.error = error;
  }

  public ERROR getError() {
    return error;
  }
}