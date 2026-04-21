package com.sprint.mission.findex.domain.autosyncconfig.controller.api;

import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncQueryCondition;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "자동 연동 설정 API")
public interface AutoSyncConfigApi {

  @Operation(summary = "자동 연동 설정 활성화 여부 수정", description = "기존 자동 연동 설정을 수정합니다.")
  ResponseEntity<AutoSyncConfigResponse> updateEnabled(
      @Parameter(description = "자동 연동 설정 ID") @PathVariable UUID id,
      @RequestBody @Valid AutoSyncConfigUpdateRequest request
  );

  @Operation(summary = "자동 연동 설정 목록 조회", description = "자동 연동 설정 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  ResponseEntity<CursorPageResponse<AutoSyncConfigResponse>> findAll(
      @Valid @ParameterObject @ModelAttribute AutoSyncQueryCondition condition
  );
}
