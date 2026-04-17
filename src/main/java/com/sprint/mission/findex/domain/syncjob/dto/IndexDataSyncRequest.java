package com.sprint.mission.findex.domain.syncjob.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "IndexDataSyncRequest", description = "지수 데이터 연동 요청")
public record IndexDataSyncRequest(

    @Schema(description = "지수 정보 ID 목록")
    @NotEmpty(message = "최소 하나 이상의 지수 정보 ID가 필요합니다.")
    List<UUID> indexInfoIds,

    @Schema(description = "대상 날짜(부터)")
    @NotNull(message = "시작일(baseDateFrom)은 필수입니다.")
    LocalDate baseDateFrom,

    @Schema(description = "대상 날짜(까지)")
    @NotNull(message = "종료일(baseDateTo)은 필수입니다.")
    LocalDate baseDateTo
) {
  public IndexDataSyncRequest {
    if (baseDateFrom != null && baseDateTo != null && baseDateFrom.isAfter(baseDateTo)) {
      throw new IllegalArgumentException("시작일은 종료일보다 미래일 수 없습니다.");
    }
  }
}