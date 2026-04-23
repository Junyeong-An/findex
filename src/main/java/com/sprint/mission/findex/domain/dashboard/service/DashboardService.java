package com.sprint.mission.findex.domain.dashboard.service;

import com.sprint.mission.findex.domain.dashboard.dto.IndexPerformancePeriodType;
import com.sprint.mission.findex.domain.dashboard.dto.IndexPerformanceResponse;
import com.sprint.mission.findex.domain.dashboard.dto.RankedIndexPerformanceQueryCondition;
import com.sprint.mission.findex.domain.dashboard.dto.RankedIndexPerformanceResponse;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import com.sprint.mission.findex.domain.indexdata.repository.IndexDataRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class DashboardService {

  private final IndexDataRepository indexDataRepository;

  public List<RankedIndexPerformanceResponse> getIndexPerformanceRank(
      RankedIndexPerformanceQueryCondition condition) {

    Map<UUID, List<IndexData>> groupedData = indexDataRepository.findPerformanceData(condition)
        .stream()
        .collect(Collectors.groupingBy(
            d -> d.getIndexInfo().getId(),
            LinkedHashMap::new,
            Collectors.toList()
        ));

    List<IndexPerformanceResponse> performances = groupedData.values().stream()
        .map(list -> calculatePerformance(list, condition.periodType()))
        .filter(Objects::nonNull)
        .sorted(
            Comparator.comparing(IndexPerformanceResponse::fluctuationRate).reversed()
                .thenComparing(IndexPerformanceResponse::indexName)
        )
        .toList();

    return assignRanks(performances, condition.limit());
  }

  private IndexPerformanceResponse calculatePerformance(List<IndexData> list,
      IndexPerformancePeriodType type) {
    if (list.size() < 2) {
      return null;
    }

    IndexData current = list.get(0);
    LocalDate targetDate = switch (type) {
      case DAILY -> current.getBaseDate().minusDays(1);
      case WEEKLY -> current.getBaseDate().minusWeeks(1);
      case MONTHLY -> current.getBaseDate().minusMonths(1);
    };

    return list.stream()
        .filter(d -> !d.getBaseDate().isAfter(targetDate))
        .findFirst()
        .map(before -> {
          BigDecimal currentPrice = current.getClosingPrice();
          BigDecimal beforePrice = before.getClosingPrice();

          if (beforePrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
          }

          BigDecimal fluctuationRate = currentPrice.subtract(beforePrice)
              .divide(beforePrice, 8, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100))
              .setScale(4, RoundingMode.HALF_UP);

          return new IndexPerformanceResponse(
              current.getIndexInfo().getId(),
              current.getIndexInfo().getIndexClassification(),
              current.getIndexInfo().getIndexName(),
              currentPrice.subtract(beforePrice),
              fluctuationRate,
              currentPrice,
              beforePrice
          );
        })
        .orElse(null);
  }

  private List<RankedIndexPerformanceResponse> assignRanks(
      List<IndexPerformanceResponse> performances, Integer limit) {
    return IntStream.range(0, Math.min(performances.size(), limit))
        .mapToObj(i -> new RankedIndexPerformanceResponse(performances.get(i), i + 1))
        .toList();
  }
}
