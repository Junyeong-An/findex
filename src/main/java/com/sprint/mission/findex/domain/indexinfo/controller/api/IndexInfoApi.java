package com.sprint.mission.findex.domain.indexinfo.controller.api;

import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoUpdateRequest;
import com.sprint.mission.findex.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "지수 정보 API")
public interface IndexInfoApi {

  @Operation(summary = "지수 정보 등록", description = "새로운 지수 정보를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "지수 정보 생성 성공",
          content = @Content(schema = @Schema(implementation = IndexInfoResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 등)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 지수 정보",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<IndexInfoResponse> create(@RequestBody @Valid IndexInfoCreateRequest req);

  @Operation(summary = "지수 정보 수정", description = "기존 지수 정보를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 정보 수정 성공",
          content = @Content(schema = @Schema(implementation = IndexInfoResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필드 값 등)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "수정할 지수 정보를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<IndexInfoResponse> update(
      @Parameter(description = "지수 정보 ID") @PathVariable UUID id,
      @RequestBody @Valid IndexInfoUpdateRequest req);

  @Operation(summary = "지수 정보 삭제", description = "지수 정보를 삭제합니다. 관련된 지수 데이터도 함께 삭제됩니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "지수 정보 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "삭제할 지수 정보를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "지수 정보 ID") @PathVariable UUID id);
}