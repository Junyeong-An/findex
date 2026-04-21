package com.sprint.mission.findex.domain.autosyncconfig.controller;

import com.sprint.mission.findex.domain.autosyncconfig.controller.api.AutoSyncConfigApi;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.findex.domain.autosyncconfig.service.AutoSyncConfigService;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncConfigController implements AutoSyncConfigApi {

  private final AutoSyncConfigService autoSyncConfigService;

  @Override
  @PatchMapping("/{id}")
  public ResponseEntity<AutoSyncConfigResponse> updateEnabled(
      @PathVariable UUID id,
      @RequestBody @Valid AutoSyncConfigUpdateRequest request
  ) {
    return ResponseEntity.ok(autoSyncConfigService.updateEnabled(id, request));
  }

  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponse<AutoSyncConfigResponse>> findAll(
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID indexInfoId,
      @RequestParam(required = false) Boolean enabled,
      @RequestParam(defaultValue = "indexInfo.indexName") String sortField,
      @RequestParam(defaultValue = "asc") String sortDirection,
      @Min(1) @RequestParam(defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(
        autoSyncConfigService.findAll(idAfter, cursor, indexInfoId, enabled, sortField, sortDirection, size)
    );
  }
}
