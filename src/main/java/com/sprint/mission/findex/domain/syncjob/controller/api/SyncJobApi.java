package com.sprint.mission.findex.domain.syncjob.controller.api;

import com.sprint.mission.findex.domain.syncjob.dto.IndexDataSyncRequest;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobSearchCondition;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Sync Job", description = "지수 데이터 연동 및 이력 관리 API (M4)")
public interface SyncJobApi {

  @Operation(summary = "지수 데이터 수동 연동", description = "지정한 기간(baseDateFrom ~ baseDateTo)의 하나 이상의 지수 데이터를 외부 API로부터 연동합니다.")
  ResponseEntity<String> syncIndexData(
      @Valid @RequestBody IndexDataSyncRequest request,
      HttpServletRequest servletRequest);

  @Operation(summary = "연동 작업 목록 조회", description = "ID 기반 커서를 사용하여 지수 데이터 연동 이력을 페이징 조회합니다.")
  ResponseEntity<CursorPageResponse<SyncJobResponse>> getSyncJobHistory(
      @Valid @ParameterObject @ModelAttribute SyncJobSearchCondition condition,
      @RequestParam(required = false) UUID cursor,
      @RequestParam(defaultValue = "10") int size);
}