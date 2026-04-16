package com.sprint.mission.findex.domain.autosyncconfig.dto;

import java.util.UUID;

public record AutoSyncConfigResponse(
    UUID id,
    UUID indexInfoId,
    String indexClassification,
    String indexName,
    boolean enabled
) {

}
