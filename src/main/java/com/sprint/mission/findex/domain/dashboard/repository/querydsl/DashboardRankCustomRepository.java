package com.sprint.mission.findex.domain.dashboard.repository.querydsl;

import com.sprint.mission.findex.domain.dashboard.dto.RankedIndexPerformanceQueryCondition;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import java.util.List;

public interface DashboardRankCustomRepository {

  List<IndexData> findPerformanceData(RankedIndexPerformanceQueryCondition condition);
}
