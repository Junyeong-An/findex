package com.sprint.mission.findex.domain.indexinfo.dto;

import ch.qos.logback.core.util.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

public record IndexInfoQueryCondition(

    @Schema(description = "지수 분류명")
    String indexClassification,

    @Schema(description = "지수명")
    String indexName,

    @Schema(description = "즐겨찾기 여부")
    Boolean favorite,

    @Schema(description = "이전 페이지 마지막 요소 ID")
    UUID idAfter,

    @Schema(description = "커서 (다음 페이지 시작점)")
    String cursor,

    @Schema(description = "정렬 필드", allowableValues = {"indexClassification", "indexName",
        "employedItemsCount"}, defaultValue = "indexClassification")
    String sortField,

    @Schema(description = "정렬 방향", allowableValues = {"asc", "desc"}, defaultValue = "asc")
    String sortDirection,

    @Schema(description = "페이지 크기", defaultValue = "10")
    @Min(10) @Max(20)
    Integer size
) {

  public IndexInfoQueryCondition {
        if (StringUtil.isNullOrEmpty(sortField)) {
            sortField = "indexClassification";
        }

        if (StringUtil.isNullOrEmpty(sortDirection)) {
            sortDirection = "asc";
        } else  {
            sortDirection = sortDirection.toLowerCase();
        }

        if (size == null) {
            size = 10;
        }
  }

  @AssertTrue(message = "cursor와 idAfter는 함께 전달되어야 합니다")
  public boolean isCursorAndIdAfterConsistent() {
    return (cursor == null) == (idAfter == null);
  }

  @AssertTrue(message = "employedItemsCount 정렬 시 cursor는 정수 형식이어야 합니다")
  public boolean isCursorValidForSortField() {
    if (!"employedItemsCount".equals(sortField) || cursor == null) {
      return true;
    }
    try {
      Integer.valueOf(cursor);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}