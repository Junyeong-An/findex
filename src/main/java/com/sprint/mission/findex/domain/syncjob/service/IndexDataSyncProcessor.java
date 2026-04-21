package com.sprint.mission.findex.domain.syncjob.service;

import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import com.sprint.mission.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import com.sprint.mission.findex.domain.syncjob.repository.SyncJobRepository;
import com.sprint.mission.findex.domain.syncjob.mapper.SyncJobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IndexDataSyncProcessor {

  private final IndexDataRepository indexDataRepository;
  private final SyncJobRepository syncJobRepository;
  private final SyncJobMapper syncJobMapper;

  @Transactional
  public SyncJobResponse saveIndexDataAndHistory(
      List<IndexData> indexDataList,
      IndexInfo indexInfo,
      LocalDate targetDate,
      String workerIp,
      String logMessage) {

    if (indexDataList != null && !indexDataList.isEmpty()) {
      indexDataRepository.saveAll(indexDataList);
    }

    SyncJob syncJob = syncJobMapper.toEntity(
        indexInfo, JobType.INDEX_DATA, targetDate, workerIp, JobResult.SUCCESS, logMessage
    );

    return syncJobMapper.toResponse(syncJobRepository.save(syncJob));
  }
}