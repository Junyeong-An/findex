package com.sprint.mission.findex.domain.syncjob.dto;

import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "연동 이력 검색 필터")
public record SyncJobQueryCondition(

    @Schema(description = "연동 작업 유형 (INDEX_INFO, INDEX_DATA)")
    JobType jobType,

    @Schema(description = "지수 정보 ID")
    UUID indexInfoId,

    @Schema(description = "대상 날짜 (부터)", example = "2024-04-01")
    LocalDate baseDateFrom,

    @Schema(description = "대상 날짜 (까지)", example = "2024-04-10")
    LocalDate baseDateTo,

    @Schema(description = "작업자")
    String worker,

    @Schema(description = "작업 일시 (부터)", example = "2024-04-01")
    LocalDate jobTimeFrom,

    @Schema(description = "작업 일시 (까지)", example = "2024-04-10")
    LocalDate jobTimeTo,

    @Schema(description = "작업 상태 (SUCCESS, FAILED)")
    JobResult status

) {
  @AssertTrue(message = "대상 날짜(부터)는 대상 날짜(까지)보다 미래일 수 없습니다.")
  public boolean isBaseDateRangeValid() {
    return baseDateFrom == null || baseDateTo == null || !baseDateFrom.isAfter(baseDateTo);
  }

  @AssertTrue(message = "작업 일시(부터)는 작업 일시(까지)보다 미래일 수 없습니다.")
  public boolean isJobTimeRangeValid() {
    return jobTimeFrom == null || jobTimeTo == null || !jobTimeFrom.isAfter(jobTimeTo);
  }
}