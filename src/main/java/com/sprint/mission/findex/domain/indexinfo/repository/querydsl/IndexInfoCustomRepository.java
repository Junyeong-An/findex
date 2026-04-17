package com.sprint.mission.findex.domain.indexinfo.repository.querydsl;

import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoQueryCondition;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;

public interface IndexInfoCustomRepository {

  CursorPageResponse<IndexInfoResponse> findIndexInfos(IndexInfoQueryCondition condition);
}
