package com.sprint.mission.findex.domain.syncjob.repository;

import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;

import java.util.UUID;

public interface SyncJobCustomRepository {

  CursorPageResponse<SyncJobResponse> searchSyncJobPage(
      SyncJobSearchCondition condition,
      String cursor,
      UUID idAfter,
      String sortField,
      String sortDirection,
      int size
  );
}