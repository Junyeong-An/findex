package com.sprint.mission.findex.domain.syncjob.repository.querydsl;

import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobQueryCondition;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;

import java.util.UUID;

public interface SyncJobCustomRepository {

  CursorPageResponse<SyncJobResponse> searchSyncJobPage(
      SyncJobQueryCondition condition,
      String cursor,
      UUID idAfter,
      String sortField,
      String sortDirection,
      int size
  );
}