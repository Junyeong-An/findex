package com.sprint.mission.findex.domain.autosyncconfig.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

@Schema(description = "자동 연동 설정 목록 조회 조건")
public record AutoSyncQueryCondition(

    @Schema(description = "지수 정보 ID 필터")
    UUID indexInfoId,

    @Schema(description = "활성화 여부 필터")
    Boolean enabled,

    @Schema(description = "이전 페이지 마지막 ID (tiebreaker)")
    UUID idAfter,

    @Schema(description = "커서 값 (정렬 필드 기준 마지막 값)")
    String cursor,

    @Schema(description = "정렬 필드 (indexInfo.indexName, enabled)", example = "indexInfo.indexName")
    String sortField,

    @Schema(description = "정렬 방향 (asc, desc)", example = "asc")
    String sortDirection,

    @Min(1) @Max(100)
    @Schema(description = "페이지 크기", example = "10")
    Integer size
) {

  private static final String DEFAULT_SORT_FIELD = "indexInfo.indexName";
  private static final String DEFAULT_SORT_DIRECTION = "asc";
  private static final int DEFAULT_SIZE = 10;

  public AutoSyncQueryCondition {
    if (sortField == null || sortField.isBlank()) {
      sortField = DEFAULT_SORT_FIELD;
    }
    if (sortDirection == null || sortDirection.isBlank()) {
      sortDirection = DEFAULT_SORT_DIRECTION;
    }
    if (size == null) {
      size = DEFAULT_SIZE;
    }
  }

  @AssertTrue(message = "cursor와 idAfter는 함께 전달되어야 합니다")
  public boolean isCursorAndIdAfterConsistent() {
    return (cursor == null) == (idAfter == null);
  }

  @AssertTrue(message = "enabled 정렬 시 cursor는 true 또는 false이어야 합니다")
  public boolean isCursorValidForSortField() {
    if (!"enabled".equals(sortField) || cursor == null) {
      return true;
    }
    return "true".equalsIgnoreCase(cursor) || "false".equalsIgnoreCase(cursor);
  }
}
