package com.sprint.mission.findex.domain.syncjob.dto;

import com.sprint.mission.findex.global.exception.ApiException;
import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "IndexDataSyncRequest", description = "지수 데이터 연동 요청")
public record IndexDataSyncRequest(

    @Schema(description = "지수 정보 ID 목록 (-1은 전체 지수 연동)")
    @NotEmpty(message = "최소 하나 이상의 지수 정보 ID가 필요합니다.")
    List<String> indexInfoIds,

    @Schema(description = "대상 날짜(부터)")
    @NotNull(message = "시작일(baseDateFrom)은 필수입니다.")
    LocalDate baseDateFrom,

    @Schema(description = "대상 날짜(까지)")
    @NotNull(message = "종료일(baseDateTo)은 필수입니다.")
    LocalDate baseDateTo
) {
  public IndexDataSyncRequest {
    boolean hasAllSentinel = indexInfoIds != null && indexInfoIds.stream().anyMatch("-1"::equals);

    if (hasAllSentinel && indexInfoIds.size() != 1) {
      throw new ApiException(ERROR.COMMON_INVALID_REQUEST);
    }

    if (!hasAllSentinel && indexInfoIds != null) {
      for (String id : indexInfoIds) {
        if (id == null || id.isBlank()) {
          throw new ApiException(ERROR.COMMON_INVALID_REQUEST);
        }
        try {
          UUID.fromString(id);
        } catch (IllegalArgumentException e) {
          throw new ApiException(ERROR.COMMON_INVALID_REQUEST);
        }
      }
    }

    if (baseDateFrom != null && baseDateTo != null && baseDateFrom.isAfter(baseDateTo)) {
      throw new ApiException(ERROR.COMMON_INVALID_REQUEST);
    }
  }
}