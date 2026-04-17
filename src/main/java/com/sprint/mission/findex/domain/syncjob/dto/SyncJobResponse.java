package com.sprint.mission.findex.domain.syncjob.dto;

import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "SyncJobDto", description = "연동 작업 DTO")
public record SyncJobResponse(
    UUID id,
    JobType jobType,
    UUID indexInfoId,
    LocalDate targetDate,
    String worker,
    Instant jobTime,
    JobResult result
) {
  public static SyncJobResponse from(SyncJob syncJob) {
    return new SyncJobResponse(
        syncJob.getId(),
        syncJob.getJobType(),
        syncJob.getIndexInfo().getId(),
        syncJob.getTargetDate(),
        syncJob.getWorker(),
        syncJob.getJobTime(),
        syncJob.getResult()
    );
  }
}