package com.sprint.mission.findex.domain.indexdata.dto;

import ch.qos.logback.core.util.StringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "지수 데이터 목록 조회 조건")
public record IndexDataQueryCondition(

    @Schema(description = "지수 정보 ID")
    UUID indexInfoId,

    @Schema(description = "시작 일자")
    LocalDate startDate,

    @Schema(description = "종료 일자")
    LocalDate endDate,

    @Schema(description = "이전 페이지 마지막 요소 ID")
    UUID idAfter,

    @Schema(description = "커서 (정렬 필드의 마지막 값)")
    String cursor,

    @Schema(description = "정렬 필드", allowableValues = {"baseDate", "marketPrice", "closingPrice",
        "highPrice", "lowPrice", "versus", "fluctuationRate", "tradingQuantity", "tradingPrice",
        "marketTotalAmount"}, defaultValue = "baseDate")
    String sortField,

    @Schema(description = "정렬 방향", allowableValues = {"asc", "desc"}, defaultValue = "desc")
    String sortDirection,

    @Schema(description = "페이지 크기", defaultValue = "10")
    @Min(1) @Max(100)
    Integer size

) {
  public IndexDataQueryCondition {
    if (StringUtil.isNullOrEmpty(sortField)) sortField = "baseDate";
    if (StringUtil.isNullOrEmpty(sortDirection)) sortDirection = "desc";
    else sortDirection = sortDirection.toLowerCase();
    if (size == null) size = 10;
  }

  @AssertTrue(message = "cursor와 idAfter는 함께 전달되어야 합니다")
  public boolean isCursorAndIdAfterConsistent() {
    return (cursor == null) == (idAfter == null);
  }

  @AssertTrue(message = "시작일은 종료일보다 미래일 수 없습니다")
  public boolean isDateRangeValid() {
    return startDate == null || endDate == null || !startDate.isAfter(endDate);
  }
}