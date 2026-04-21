package com.sprint.mission.findex.domain.dashboard.service;

import static com.sprint.mission.findex.global.exception.ApiException.ERROR.INDEX_INFO_NOT_FOUND;

import com.sprint.mission.findex.domain.dashboard.dto.ChartDataPoint;
import com.sprint.mission.findex.domain.dashboard.dto.IndexChartPeriodType;
import com.sprint.mission.findex.domain.dashboard.dto.IndexChartResponse;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import com.sprint.mission.findex.domain.indexdata.repository.IndexDataRepository;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.indexinfo.repository.IndexInfoRepository;
import com.sprint.mission.findex.global.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardChartService {

  private final IndexInfoRepository indexInfoRepository;
  private final IndexDataRepository indexDataRepository;

  public IndexChartResponse getIndexChart(
      UUID id,
      IndexChartPeriodType periodType
  ) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new ApiException(INDEX_INFO_NOT_FOUND));

    LocalDate fromDate = getFromDate(periodType);

    List<IndexData> sortedIndexData = indexDataRepository
        .findByIndexInfoIdAndBaseDateBetween(id, fromDate, LocalDate.now())
        .stream()
        .sorted(Comparator.comparing(IndexData::getBaseDate))
        .toList();

    List<ChartDataPoint> dataPoints = toChartDataPoints(sortedIndexData);
    List<ChartDataPoint> ma5DataPoints = calculateMovingAverage(sortedIndexData, 5);
    List<ChartDataPoint> ma20DataPoints = calculateMovingAverage(sortedIndexData, 20);

    return new IndexChartResponse(
        indexInfo.getId(),
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        periodType,
        dataPoints,
        ma5DataPoints,
        ma20DataPoints
    );
  }

  private LocalDate getFromDate(IndexChartPeriodType periodType) {
    LocalDate today = LocalDate.now();

    return switch (periodType) {
      case MONTHLY -> today.minusMonths(1);
      case QUARTERLY -> today.minusMonths(3);
      case YEARLY -> today.minusYears(1);
    };
  }

  private List<ChartDataPoint> toChartDataPoints(List<IndexData> sortedIndexData) {
    return sortedIndexData.stream()
        .map(indexData -> new ChartDataPoint(
            indexData.getBaseDate(),
            indexData.getClosingPrice()
        ))
        .toList();
  }

  private List<ChartDataPoint> calculateMovingAverage(
      List<IndexData> sortedIndexData,
      int windowSize
  ) {
    List<ChartDataPoint> result = new ArrayList<>();
    BigDecimal winSum = BigDecimal.ZERO;
    int i;
    // 초기 윈도우 채우기: 첫 번째 MA 계산에 필요한 windowSize-1개 선합산
    for (i = 0; i < windowSize - 1 && i < sortedIndexData.size(); i++) {
      winSum = winSum.add(sortedIndexData.get(i).getClosingPrice());
    }
    // 슬라이딩 윈도우: 새 값 추가 → MA 계산 → 가장 오래된 값 제거
    for (; i < sortedIndexData.size(); i++) {
      winSum = winSum.add(sortedIndexData.get(i).getClosingPrice());
      result.add(new ChartDataPoint(
          sortedIndexData.get(i).getBaseDate(),
          winSum.divide(BigDecimal.valueOf(windowSize), 4, RoundingMode.HALF_UP)
      ));
      winSum = winSum.subtract(sortedIndexData.get(i - windowSize + 1).getClosingPrice());
    }
    return result;
  }
}
