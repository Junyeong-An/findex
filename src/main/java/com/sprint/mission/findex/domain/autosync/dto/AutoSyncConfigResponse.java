package com.sprint.mission.findex.domain.autosync.dto;

import com.sprint.mission.findex.domain.autosync.entity.AutoSyncConfig;
import java.time.Instant;
import java.util.UUID;

public record AutoSyncConfigResponse(
    UUID id,
    UUID indexInfoId,
    String indexClassification,
    String indexName,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {

  public static AutoSyncConfigResponse from(AutoSyncConfig config) {
    return new AutoSyncConfigResponse(
        config.getId(),
        config.getIndexInfo().getId(),
        config.getIndexInfo().getIndexClassification(),
        config.getIndexInfo().getIndexName(),
        config.isEnabled(),
        config.getCreatedAt(),
        config.getUpdatedAt()
    );
  }
}
