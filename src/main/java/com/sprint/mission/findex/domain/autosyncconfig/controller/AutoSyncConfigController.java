package com.sprint.mission.findex.domain.autosyncconfig.controller;

import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.findex.domain.autosyncconfig.service.AutoSyncConfigService;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
      @RequestParam(required = false) UUID cursor,
      @RequestParam(required = false) UUID indexInfoId,
      @RequestParam(required = false) Boolean enabled,
      @RequestParam(defaultValue = "10") int size
  ) {
    return ResponseEntity.ok(
        autoSyncConfigService.findAll(idAfter, cursor, indexInfoId, enabled, size)
    );
  }
}
