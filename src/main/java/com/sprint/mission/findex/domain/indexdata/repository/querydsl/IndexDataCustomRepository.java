package com.sprint.mission.findex.domain.indexdata.repository.querydsl;

import com.sprint.mission.findex.domain.indexdata.dto.IndexDataQueryCondition;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;

public interface IndexDataCustomRepository {
  CursorPageResponse<IndexDataResponse> findAll(IndexDataQueryCondition request);
}