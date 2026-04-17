package com.sprint.mission.findex.domain.syncjob.controller;

import com.sprint.mission.findex.domain.syncjob.controller.api.SyncJobApi;
import com.sprint.mission.findex.domain.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.findex.domain.syncjob.service.SyncJobService;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobApi {

  private final SyncJobService syncJobService;

  @PostMapping("/index-data")
  @Override
  public ResponseEntity<String> syncIndexData(
      @Valid @RequestBody IndexDataSyncRequest request,
      HttpServletRequest servletRequest) {

    String workerIp = servletRequest.getRemoteAddr();

    syncJobService.syncIndexData(
        request.indexInfoIds(),
        request.baseDateFrom(),
        request.baseDateTo(),
        workerIp
    );
    return ResponseEntity.ok("데이터 연동이 성공적으로 완료되었습니다.");
  }

  @GetMapping
  @Override
  public ResponseEntity<CursorPageResponse<SyncJobResponse>> getSyncJobHistory(
      @Valid @ParameterObject @ModelAttribute SyncJobSearchCondition condition,
      @RequestParam(required = false) UUID cursor,
      @RequestParam(defaultValue = "10") int size) {

    CursorPageResponse<SyncJobResponse> response = syncJobService.getSyncJobHistory(
        condition, cursor, size
    );

    return ResponseEntity.ok(response);
  }
}