package com.sprint.mission.findex.domain.autosync.dto;

import jakarta.validation.constraints.NotNull;

public record AutoSyncConfigUpdateRequest(
    @NotNull Boolean enabled
) {

}
