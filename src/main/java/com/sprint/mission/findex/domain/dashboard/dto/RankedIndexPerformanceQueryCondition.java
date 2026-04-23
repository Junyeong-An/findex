package com.sprint.mission.findex.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

public record RankedIndexPerformanceQueryCondition(
    @Schema(description = "지수 정보 ID")
    UUID indexInfoId,

    @Schema(description = "성과 기간 유형 (DAILY, WEEKLY, MONTHLY)", allowableValues = {
        "DAILY", "WEEKLY", "MONTHLY"}, defaultValue = "DAILY")
    IndexPerformancePeriodType periodType,

    @Schema(description = "최대 랭킹 수", defaultValue = "10")
    @Min(10) @Max(100)
    Integer limit
) {

  public RankedIndexPerformanceQueryCondition {
    if (periodType == null) {
      periodType = IndexPerformancePeriodType.DAILY;
    }

    if (limit == null) {
      limit = 10;
    }
  }
}
