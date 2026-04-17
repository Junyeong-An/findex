package com.sprint.mission.findex.domain.syncjob.dto;

import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import jakarta.validation.constraints.AssertTrue;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// 연동 이력 검색 필터용
public record SyncJobSearchCondition(
    JobType jobType,
    UUID indexInfoId,
    LocalDate baseDateFrom,
    LocalDate baseDateTo,
    String worker,
    Instant jobTimeFrom,
    Instant jobTimeTo,
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