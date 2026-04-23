package com.sprint.mission.findex.domain.dashboard.repository.querydsl.impl;

import static com.sprint.mission.findex.domain.indexdata.entity.QIndexData.indexData;
import static com.sprint.mission.findex.domain.indexinfo.entity.QIndexInfo.indexInfo;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.findex.domain.dashboard.dto.IndexPerformancePeriodType;
import com.sprint.mission.findex.domain.dashboard.dto.RankedIndexPerformanceQueryCondition;
import com.sprint.mission.findex.domain.dashboard.repository.querydsl.DashboardRankCustomRepository;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class DashboardRankCustomRepositoryImpl implements DashboardRankCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Value("${sync.fallback-limit-days:14}")
  private int fallbackLimitDays;

  @Override
  public List<IndexData> findPerformanceData(RankedIndexPerformanceQueryCondition condition) {
    return queryFactory
        .selectFrom(indexData)
        .join(indexData.indexInfo, indexInfo).fetchJoin()
        .where(
            eqIndexInfoId(condition.indexInfoId()),
            betweenBaseDate(condition.periodType())
        )
        .orderBy(indexData.baseDate.desc())
        .fetch();
  }

  private BooleanExpression eqIndexInfoId(UUID id) {
    return id != null ? indexData.indexInfo.id.eq(id) : null;
  }

  private BooleanExpression betweenBaseDate(IndexPerformancePeriodType type) {
    LocalDate to = LocalDate.now();
    LocalDate from = switch (type) {
      case WEEKLY -> to.minusWeeks(1).minusDays(fallbackLimitDays);
      case MONTHLY -> to.minusMonths(1).minusDays(fallbackLimitDays);
      default -> to.minusDays(fallbackLimitDays);
    };
    return indexData.baseDate.between(from, to);
  }
}
