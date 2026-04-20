package com.sprint.mission.findex.domain.syncjob.mapper;

import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncJobMapper {
  @Mapping(target = "worker", source = "workerIp")
  @Mapping(target = "errorMessage", source = "logMessage")
  SyncJob toEntity(IndexInfo indexInfo, JobType jobType, LocalDate targetDate, String workerIp, JobResult result, String logMessage);
  @Mapping(target = "indexInfoId", source = "indexInfo.id")
  SyncJobResponse toResponse(SyncJob syncJob);
}