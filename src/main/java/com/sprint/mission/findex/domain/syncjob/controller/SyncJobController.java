package com.sprint.mission.findex.domain.syncjob.controller;

import com.sprint.mission.findex.domain.syncjob.controller.api.SyncJobApi;
import com.sprint.mission.findex.domain.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobQueryCondition;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.service.SyncJobService;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobApi {

  private final SyncJobService syncJobService;

  @PostMapping("/index-infos")
  @Override
  public ResponseEntity<List<SyncJobResponse>> syncIndexInfos(
      @RequestParam(required = false) LocalDate targetDate,
      HttpServletRequest servletRequest) {

    String workerIp = maskIp(servletRequest.getRemoteAddr());
    List<SyncJobResponse> results = syncJobService.syncIndexInfos(targetDate, workerIp);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(results);
  }

  @PostMapping("/index-data")
  @Override
  public ResponseEntity<List<SyncJobResponse>> syncIndexData(
      @Valid @RequestBody IndexDataSyncRequest request,
      HttpServletRequest servletRequest) {

    String workerIp = maskIp(servletRequest.getRemoteAddr());
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
      @Valid @ParameterObject @ModelAttribute SyncJobQueryCondition condition) {

    return ResponseEntity.ok(syncJobService.getSyncJobHistory(condition));
  }

  private String maskIp(String ip) {
    if (ip == null) return "unknown";
    if (ip.contains(":")) {
      String[] parts = ip.split(":", -1);
      if (parts.length >= 2) {
        return parts[0] + ":" + parts[1] + ":*:*:*:*:*:*";
      }
      return "*:*:*:*:*:*:*:*";
    }
    String[] parts = ip.split("\\.", -1);
    if (parts.length == 4) {
      return parts[0] + "." + parts[1] + ".*.*";
    }
    return "*.*.*.*";
  }
}
