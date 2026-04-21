package com.sprint.mission.findex.domain.syncjob.service;

import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import com.sprint.mission.findex.domain.indexdata.mapper.IndexDataMapper;
import com.sprint.mission.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoUpdateRequest;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.indexinfo.repository.IndexInfoRepository;
import com.sprint.mission.findex.domain.syncclient.client.KrxOpenApiClient;
import com.sprint.mission.findex.domain.syncclient.dto.IndexDataApiResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobQueryCondition;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import com.sprint.mission.findex.domain.syncjob.repository.SyncJobRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import com.sprint.mission.findex.global.exception.ApiException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncJobService {

  private final SyncJobRepository syncJobRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;
  private final KrxOpenApiClient krxOpenApiClient;
  private final IndexDataMapper indexDataMapper;
  private final IndexInfoSyncProcessor indexInfoSyncProcessor;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  @Value("${sync.default-sync-days:7}")
  private int defaultSyncDays;

  @Value("${sync.fallback-limit-days:14}")
  private int fallbackLimitDays;

  private final IndexDataSyncProcessor indexDataSyncProcessor;

  public List<SyncJobResponse> syncIndexInfos(LocalDate targetDate, String workerIp) {
    List<IndexDataApiResponse> responses = List.of();

    if (targetDate != null) {
      responses = krxOpenApiClient.fetchByDateRange(null, targetDate, targetDate);
    } else {
      for (int i = 0; i <= defaultSyncDays; i++) {
        LocalDate candidate = LocalDate.now(KST).minusDays(i);
        try {
          responses = krxOpenApiClient.fetchByDateRange(null, candidate, candidate);
          if (!responses.isEmpty()) {
            targetDate = candidate;
            break;
          }
        } catch (ApiException e) {
          log.warn("[IndexInfo Sync] {} 데이터 조회 실패, 다음 날짜로 탐색: {}", candidate, e.getMessage());
        }
      }
      if (responses.isEmpty()) {
        log.warn("[IndexInfo Sync] 최근 {}일 이내 데이터 없음, 최대 {}일까지 확장 탐색", defaultSyncDays, fallbackLimitDays);
        for (int i = defaultSyncDays + 1; i <= fallbackLimitDays; i++) {
          LocalDate candidate = LocalDate.now(KST).minusDays(i);
          try {
            responses = krxOpenApiClient.fetchByDateRange(null, candidate, candidate);
            if (!responses.isEmpty()) {
              targetDate = candidate;
              break;
            }
          } catch (ApiException e) {
            log.warn("[IndexInfo Sync] {} 데이터 조회 실패, 다음 날짜로 탐색: {}", candidate, e.getMessage());
          }
        }
      }
    }

    if (responses.isEmpty()) {
      throw new ApiException(ApiException.ERROR.SYNC_JOB_OPEN_API_ERROR);
    }

    List<SyncJobResponse> results = new ArrayList<>();

    for (IndexDataApiResponse response : responses) {
      Optional<IndexInfo> existing = indexInfoRepository.findByIndexClassificationAndIndexName(
          response.idxCsf(), response.idxNm()
      );

      if (existing.isEmpty()) {
        try {
          results.add(indexInfoSyncProcessor.createAndSaveHistory(toCreateRequest(response), targetDate, workerIp));
          log.info("[IndexInfo Sync 성공-신규] 지수: {}", response.idxNm());
        } catch (Exception e) {
          String errorMsg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();

          Optional<IndexInfo> retryExisting = indexInfoRepository.findByIndexClassificationAndIndexName(
              response.idxCsf(), response.idxNm()
          );

          if (retryExisting.isPresent()) {
            log.error("[IndexInfo Sync 실패-신규(충돌 의심)] 지수: {}, 사유: {}", response.idxNm(), errorMsg);
            results.add(saveSyncJobHistory(retryExisting.get(), JobType.INDEX_INFO, targetDate, workerIp, JobResult.FAILED, "신규 생성 중 예외 발생 (Unique 제약조건 충돌 의심): " + errorMsg));
          } else {
            log.error("[IndexInfo Sync 비정상 흐름] 워크플로우 위반 가능성 탐지 지수명: {} | 사유: {} | 조치: 해당 지수의 사전 등록 여부 및 마스터 데이터 확인 필요",
                response.idxNm(), errorMsg);
          }
        }
      } else {
        IndexInfo indexInfo = existing.get();
        try {
          results.add(indexInfoSyncProcessor.updateAndSaveHistory(indexInfo, toUpdateRequest(response), targetDate, workerIp));
          log.info("[IndexInfo Sync 성공-갱신] 지수: {}", response.idxNm());
        } catch (Exception e) {
          log.error("[IndexInfo Sync 실패-갱신] 지수: {}, 사유: {}", response.idxNm(), e.getMessage());
          results.add(saveSyncJobHistory(indexInfo, JobType.INDEX_INFO, targetDate, workerIp, JobResult.FAILED, e.getMessage()));
        }
      }
    }
    return results;
  }
  public List<SyncJobResponse> syncIndexData(List<UUID> indexInfoIds, LocalDate baseDateFrom, LocalDate baseDateTo, String workerIp) {

    if (baseDateFrom.isAfter(baseDateTo)) {
      throw new ApiException(ApiException.ERROR.COMMON_INVALID_REQUEST);
    }

    boolean isSingleDay = baseDateFrom.isEqual(baseDateTo);
    List<SyncJobResponse> results = new ArrayList<>();

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

        int dataSize = externalDataList.size();

        if (dataSize == 0) {
          throw new ApiException(ApiException.ERROR.INDEX_DATA_NOT_FOUND);
        }

        LocalDate actualTargetDate = baseDateTo;

        Set<LocalDate> existingDates = indexDataRepository
            .findByIndexInfoIdAndBaseDateBetween(indexInfo.getId(), baseDateFrom, baseDateTo)
            .stream()
            .map(IndexData::getBaseDate)
            .collect(Collectors.toSet());

        List<IndexData> indexDataList = indexDataMapper.toEntityList(externalDataList, indexInfo)
            .stream()
            .filter(d -> !existingDates.contains(d.getBaseDate()))
            .collect(Collectors.toList());

        if (!indexDataList.isEmpty()) {
          actualTargetDate = indexDataList.stream()
              .map(IndexData::getBaseDate)
              .max(LocalDate::compareTo)
              .orElse(baseDateTo);
        }

        String logMessage = isSingleDay
            ? null
            : String.format("범위 연동: %s ~ %s (%d건)", baseDateFrom, baseDateTo, indexDataList.size());

        results.add(indexDataSyncProcessor.saveIndexDataAndHistory(
            indexDataList, indexInfo, actualTargetDate, workerIp, logMessage));
        log.info("[Sync 성공] 지수: {}, 요청범위: {} ~ {} -> 실제연동기준일: {} ({}건)",
            indexInfo.getIndexName(), baseDateFrom, baseDateTo, actualTargetDate, indexDataList.size());

      } catch (Exception e) {
        String safeErrorMsg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();

        String errorLog = isSingleDay
            ? String.format("[단건 연동 실패] 해당 지수 외 정상 처리됨 | 사유: %s", safeErrorMsg)
            : String.format("[범위 연동 실패] 기간: %s ~ %s | 타 데이터 영향 없음 | 사유: %s", baseDateFrom, baseDateTo, safeErrorMsg);

        log.error("[IndexData Sync 부분 실패] 지수: {} | {}", indexInfo.getIndexName(), errorLog);

        try {
          results.add(saveSyncJobHistory(indexInfo, JobType.INDEX_DATA, baseDateTo, workerIp, JobResult.FAILED, errorLog));
        } catch (Exception historyEx) {
          log.error("[IndexData Sync 실패 이력 저장 실패] 지수: {} | 사유: {}", indexInfo.getIndexName(), historyEx.getMessage());
        }
      }
    }
    return results;
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<SyncJobResponse> getSyncJobHistory(SyncJobQueryCondition condition) {
    return syncJobRepository.searchSyncJobPage(
        condition, condition.cursor(), condition.idAfter(),
        condition.sortField(), condition.sortDirection(), condition.size());
  }

  private IndexInfoCreateRequest toCreateRequest(IndexDataApiResponse response) {
    return new IndexInfoCreateRequest(
        response.idxCsf(),
        response.idxNm(),
        response.epyItmsCnt(),
        parseDate(response.basPntm()),
        response.basIdx(),
        false
    );
  }

  private IndexInfoUpdateRequest toUpdateRequest(IndexDataApiResponse response) {
    return new IndexInfoUpdateRequest(
        response.epyItmsCnt(),
        parseDate(response.basPntm()),
        response.basIdx(),
        null
    );
  }

  private LocalDate parseDate(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) {
      throw new ApiException(ApiException.ERROR.COMMON_INVALID_REQUEST);
    }
    try {
      return LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
    } catch (DateTimeParseException e) {
      throw new ApiException(ApiException.ERROR.COMMON_INVALID_REQUEST);
    }
  }

  private SyncJobResponse saveSyncJobHistory(IndexInfo indexInfo, JobType jobType, LocalDate targetDate,
      String worker, JobResult result, String errorMessage) {
    SyncJob syncJob = SyncJob.builder()
        .indexInfo(indexInfo)
        .jobType(jobType)
        .targetDate(targetDate)
        .worker(worker)
        .result(result)
        .errorMessage(errorMessage)
        .build();
    return SyncJobResponse.from(syncJobRepository.save(syncJob));
  }
}