package com.sprint.mission.findex.domain.autosyncconfig.repository;

import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import java.util.List;
import java.util.UUID;

public interface AutoSyncConfigRepositoryCustom {

  List<AutoSyncConfig> findAllWithCursor(
      String cursor,
      UUID idAfter,
      UUID indexInfoId,
      Boolean enabled,
      String sortField,
      boolean asc,
      int size
  );

  long countWithFilter(UUID indexInfoId, Boolean enabled);
}
