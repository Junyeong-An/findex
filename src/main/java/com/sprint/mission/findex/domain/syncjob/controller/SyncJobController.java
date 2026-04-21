package com.sprint.mission.findex.domain.syncjob.controller;

import com.sprint.mission.findex.domain.syncjob.controller.api.SyncJobApi;
import com.sprint.mission.findex.domain.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.findex.domain.syncjob.dto.IndexInfoSyncRequest;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobQueryCondition;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.service.SyncJobService;
import com.sprint.mission.findex.global.common.dto.CursorPageRequest;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobApi {

  private final SyncJobService syncJobService;

  @PostMapping("/index-infos")
  @Override
  public ResponseEntity<List<SyncJobResponse>> syncIndexInfos(
      @Valid @RequestBody IndexInfoSyncRequest request,
      HttpServletRequest servletRequest) {

    String workerIp = servletRequest.getRemoteAddr();
    List<SyncJobResponse> results = syncJobService.syncIndexInfos(request.targetDate(), workerIp);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(results);
  }

  @PostMapping("/index-data")
  @Override
  public ResponseEntity<List<SyncJobResponse>> syncIndexData(
      @Valid @RequestBody IndexDataSyncRequest request,
      HttpServletRequest servletRequest) {

    String workerIp = servletRequest.getRemoteAddr();
    List<SyncJobResponse> results = syncJobService.syncIndexData(
        request.indexInfoIds(),
        request.baseDateFrom(),
        request.baseDateTo(),
        workerIp
    );
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(results);
  }

  @GetMapping
  @Override
  public ResponseEntity<CursorPageResponse<SyncJobResponse>> getSyncJobHistory(
      @Valid @ParameterObject @ModelAttribute SyncJobQueryCondition condition,
      @Valid @ParameterObject @ModelAttribute CursorPageRequest pageRequest) {

    CursorPageResponse<SyncJobResponse> response =
        syncJobService.getSyncJobHistory(condition, pageRequest);

    return ResponseEntity.ok(response);
  }
}
