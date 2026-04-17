package com.sprint.mission.findex.domain.autosyncconfig.service;

import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.autosyncconfig.repository.AutoSyncConfigRepository;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import com.sprint.mission.findex.domain.indexdata.mapper.IndexDataMapper;
import com.sprint.mission.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.syncclient.dto.IndexDataApiResponse;
import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import com.sprint.mission.findex.domain.syncjob.repository.SyncJobRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBatchService {

  private static final String SYSTEM_WORKER = "system";

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexDataRepository indexDataRepository;
  private final SyncJobRepository syncJobRepository;
  private final IndexDataMapper indexDataMapper;

  @Transactional(readOnly = true)
  public List<AutoSyncConfig> findEnabledConfigs() {
    return autoSyncConfigRepository.findAllByEnabled(true);
  }

  @Transactional(readOnly = true)
  public Optional<LocalDate> findLastSuccessDate(UUID indexInfoId) {
    return syncJobRepository.findLastSuccessDate(indexInfoId, JobType.INDEX_DATA);
  }

  @Transactional
  public void saveIndexDataAndRecordSuccess(IndexInfo indexInfo,
      List<IndexDataApiResponse> responses, LocalDate from, LocalDate to) {
    List<IndexData> indexDataList = indexDataMapper.toEntityList(responses, indexInfo);

    // 실제 API 응답 기준 마지막 날짜 (장 마감 전 등으로 to까지 데이터가 없을 수 있음)
    LocalDate actualLastDate = indexDataList.stream()
        .map(IndexData::getBaseDate)
        .max(Comparator.naturalOrder())
        .orElse(to);

    // DB 조회를 루프 밖에서 한 번만 수행하여 N+1 방지, DB 중복 + API 응답 내부 중복(페이지 겹침 등) 모두 제거
    Set<LocalDate> seenDates = indexDataRepository
        .findByIndexInfoIdAndBaseDateBetween(indexInfo.getId(), from, to)
        .stream()
        .map(IndexData::getBaseDate)
        .collect(Collectors.toCollection(HashSet::new));
    List<IndexData> toSave = new ArrayList<>();

    for (IndexData indexData : indexDataList) {
      if (seenDates.add(indexData.getBaseDate())) {
        toSave.add(indexData);
      }
    }

    indexDataRepository.saveAll(toSave);

    syncJobRepository.save(SyncJob.builder()
        .indexInfo(indexInfo)
        .jobType(JobType.INDEX_DATA)
        .targetDate(actualLastDate)
        .worker(SYSTEM_WORKER)
        .result(JobResult.SUCCESS)
        .build());

    log.info("지수 데이터 자동 연동 성공 - indexName: {}, from: {}, to: {}, actualLastDate: {}, savedCount: {}",
        indexInfo.getIndexName(), from, to, actualLastDate, toSave.size());
  }

  @Transactional
  public void recordFailure(IndexInfo indexInfo, LocalDate targetDate, String errorMessage) {
    syncJobRepository.save(SyncJob.builder()
        .indexInfo(indexInfo)
        .jobType(JobType.INDEX_DATA)
        .targetDate(targetDate)
        .worker(SYSTEM_WORKER)
        .result(JobResult.FAILED)
        .errorMessage(errorMessage)
        .build());
  }
}
