package com.sprint.mission.findex.domain.syncjob.service;

import com.sprint.mission.findex.domain.indexdata.mapper.IndexDataMapper;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.indexinfo.repository.IndexInfoRepository;
import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import com.sprint.mission.findex.domain.syncjob.repository.SyncJobRepository;
import com.sprint.mission.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;

import com.sprint.mission.findex.domain.syncclient.client.KrxOpenApiClient;
import com.sprint.mission.findex.domain.syncclient.dto.IndexDataApiResponse;

import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobServiceImpl implements SyncJobService {

  private final SyncJobRepository syncJobRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;

  private final KrxOpenApiClient krxOpenApiClient;
  private final IndexDataMapper indexDataMapper;

  @Override
  public void syncIndexData(List<UUID> indexInfoIds, LocalDate baseDateFrom, LocalDate baseDateTo, String workerIp) {

    if (baseDateFrom.isAfter(baseDateTo)) {
      throw new IllegalArgumentException("시작일은 종료일보다 미래일 수 없습니다.");
    }

    boolean isSingleDay = baseDateFrom.isEqual(baseDateTo);

    for (UUID indexInfoId : indexInfoIds) {

      IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId).orElse(null);
      if (indexInfo == null) {
        log.error("[Sync 실패] 존재하지 않는 지수입니다. ID: {}", indexInfoId);
        continue;
      }

      try {
        List<IndexDataApiResponse> externalDataList = krxOpenApiClient.fetchByDateRange(
            indexInfo.getIndexName(),
            baseDateFrom,
            baseDateTo
        );

        int dataSize = (externalDataList != null) ? externalDataList.size() : 0;

        if (dataSize > 0) {
          List<IndexData> indexDataList = indexDataMapper.toEntityList(externalDataList, indexInfo);
          saveData(indexDataList);
        }

        String logMessage = isSingleDay
            ? null
            : String.format("범위 연동: %s ~ %s (%d건)", baseDateFrom, baseDateTo, dataSize);

        saveSyncJobHistory(indexInfo, JobType.INDEX_DATA, baseDateTo, workerIp, JobResult.SUCCESS, logMessage);
        log.info("[Sync 성공] 지수: {}, 날짜/범위: {} ~ {} ({}건)", indexInfo.getIndexName(), baseDateFrom, baseDateTo, dataSize);

      } catch (Exception e) {
        String errorLog = isSingleDay
            ? e.getMessage()
            : String.format("범위 연동 실패 (%s ~ %s): %s", baseDateFrom, baseDateTo, e.getMessage());

        log.error("[Sync 실패] 지수: {}, 사유: {}", indexInfo.getIndexName(), errorLog);

        saveSyncJobHistory(indexInfo, JobType.INDEX_DATA, baseDateTo, workerIp, JobResult.FAILED, errorLog);
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<SyncJobResponse> getSyncJobHistory(
      SyncJobSearchCondition condition,
      UUID cursor,
      int size) {

    PageRequest pageRequest = PageRequest.of(0, size + 1);

    List<SyncJob> syncJobs = syncJobRepository.searchSyncJobs(condition, cursor, pageRequest);

    boolean hasNext = syncJobs.size() > size;

    List<SyncJobResponse> content = syncJobs.stream()
        .limit(size)
        .map(SyncJobResponse::from)
        .toList();

    UUID nextCursor = null;

    if (!content.isEmpty()) {
      SyncJobResponse lastElement = content.get(content.size() - 1);
      nextCursor = lastElement.id();
    }

    return CursorPageResponse.of(
        content,
        nextCursor,
        null,
        size,
        null,
        hasNext
    );
  }

  @Transactional
  protected void saveData(List<IndexData> indexDataList) {
    indexDataRepository.saveAll(indexDataList);
  }

  private void saveSyncJobHistory(IndexInfo indexInfo, JobType jobType, LocalDate targetDate,
      String worker, JobResult result, String errorMessage) {
    SyncJob syncJob = SyncJob.builder()
        .indexInfo(indexInfo)
        .jobType(jobType)
        .targetDate(targetDate)
        .worker(worker)
        .result(result)
        .errorMessage(errorMessage)
        .build();

    syncJobRepository.save(syncJob);
  }
}
