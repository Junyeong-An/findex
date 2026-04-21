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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  @Value("${sync.default-sync-days}")
  private int defaultSyncDays;

  private final KrxOpenApiClient krxOpenApiClient;
  private final SyncBatchService syncBatchService;

  @PostConstruct
  public void validateDefaultSyncDays() {
    if (defaultSyncDays <= 0) {
      throw new IllegalStateException(
          "sync.default-sync-days 값이 유효하지 않습니다: " + defaultSyncDays + " (1 이상이어야 합니다)");
    }
  }

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
        .orElse(to.minusDays(defaultSyncDays));

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
        // 주말 빈 응답은 정상 - SUCCESS 기록으로 다음 실행 시 중복 조회 방지
        syncBatchService.recordSuccess(indexInfo, to);
      } else {
        log.warn("조회된 지수 데이터 없음 (평일) - 지수명이 올바르지 않거나 공휴일일 수 있음 - indexName: {}, from: {}, to: {}",
            indexInfo.getIndexName(), from, to);
      }
      return;
    }

    syncBatchService.saveIndexDataAndRecordSuccess(indexInfo, responses, from, to);
  }
}
