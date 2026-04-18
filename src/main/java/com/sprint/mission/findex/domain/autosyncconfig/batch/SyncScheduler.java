package com.sprint.mission.findex.domain.autosyncconfig.batch;

import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.autosyncconfig.service.SyncBatchService;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.syncclient.client.KrxOpenApiClient;
import com.sprint.mission.findex.domain.syncclient.dto.IndexDataApiResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {

  private static final int DEFAULT_SYNC_DAYS = 7;
  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private final KrxOpenApiClient krxOpenApiClient;
  private final SyncBatchService syncBatchService;

  @Scheduled(cron = "${sync.cron}", zone = "Asia/Seoul")
  public void syncIndexData() {
    List<AutoSyncConfig> configs = syncBatchService.findEnabledConfigs();
    log.info("자동 연동 배치 시작 - 대상 지수 수: {}", configs.size());

    LocalDate to = LocalDate.now(KST);
    for (AutoSyncConfig config : configs) {
      IndexInfo indexInfo = config.getIndexInfo();
      try {
        syncForIndexInfo(indexInfo, to);
      } catch (Exception e) {
        log.error("지수 데이터 자동 연동 실패 - indexName: {}, error: {}",
            indexInfo.getIndexName(), e.getMessage(), e);
        try {
          syncBatchService.recordFailure(indexInfo, to, e.getMessage());
        } catch (Exception recordEx) {
          log.error("FAILED 기록 중 오류 - indexName: {}", indexInfo.getIndexName(), recordEx);
        }
      }
    }

    log.info("자동 연동 배치 완료");
  }

  private void syncForIndexInfo(IndexInfo indexInfo, LocalDate to) {
    LocalDate from = syncBatchService
        .findLastSuccessDate(indexInfo.getId())
        .map(date -> date.plusDays(1))
        .orElse(to.minusDays(DEFAULT_SYNC_DAYS));

    if (from.isAfter(to)) {
      log.info("이미 최신 데이터 - indexName: {}", indexInfo.getIndexName());
      return;
    }

    List<IndexDataApiResponse> responses =
        krxOpenApiClient.fetchByDateRange(indexInfo.getIndexName(), from, to);

    if (responses.isEmpty()) {
      DayOfWeek dayOfWeek = to.getDayOfWeek();
      boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
      if (isWeekend) {
        log.info("조회된 지수 데이터 없음 (주말) - indexName: {}, from: {}, to: {}",
            indexInfo.getIndexName(), from, to);
      } else {
        log.warn("조회된 지수 데이터 없음 (평일) - 지수명이 올바르지 않거나 공휴일일 수 있음 - indexName: {}, from: {}, to: {}",
            indexInfo.getIndexName(), from, to);
      }
      return;
    }

    syncBatchService.saveIndexDataAndRecordSuccess(indexInfo, responses, from, to);
  }
}
