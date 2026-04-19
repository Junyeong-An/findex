package com.sprint.mission.findex.domain.indexdata.controller.api;

import com.sprint.mission.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataExportRequest;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataQueryCondition;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import com.sprint.mission.findex.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
public interface IndexDataApi {

  @Operation(summary = "지수 데이터 등록", description = "새로운 지수 데이터를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "지수 데이터 생성 성공",
          content = @Content(schema = @Schema(implementation = IndexDataResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 데이터 값 등)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "참조하는 지수 정보를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 지수 데이터",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<IndexDataResponse> create(@RequestBody @Valid IndexDataCreateRequest request);

  @Operation(summary = "지수 데이터 수정", description = "기존 지수 데이터를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 데이터 수정 성공",
          content = @Content(schema = @Schema(implementation = IndexDataResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 데이터 값 등)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "수정할 지수 데이터를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<IndexDataResponse> update(
      @Parameter(description = "지수 데이터 ID") @PathVariable UUID id,
      @RequestBody @Valid IndexDataUpdateRequest request);

  @Operation(summary = "지수 데이터 삭제", description = "지수 데이터를 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "지수 데이터 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "삭제할 지수 데이터를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "지수 데이터 ID") @PathVariable UUID id);

  @Operation(summary = "지수 데이터 목록 조회", description = "지수 데이터 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 데이터 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CursorPageResponse<IndexDataResponse>> getList(
      @ModelAttribute IndexDataQueryCondition request);

  @Operation(summary = "지수 데이터 CSV Export", description = "지수 데이터를 CSV 파일로 다운로드합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "CSV 파일 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  void exportCsv(
      @ModelAttribute IndexDataExportRequest request,
      HttpServletResponse response) throws IOException;
}