package com.sprint.mission.findex.domain.indexdata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "지수 데이터 목록 조회 조건")
public record IndexDataQueryCondition(

    @Schema(description = "지수 정보 ID", example = "9b19a600-16d4-41d1-9d36-6408a5feaad8")
    UUID indexInfoId,

    @Schema(description = "시작 일자", example = "2024-01-01")
    LocalDate startDate,

    @Schema(description = "종료 일자", example = "2024-12-31")
    LocalDate endDate,

    @Schema(description = "이전 페이지 마지막 요소 ID")
    UUID idAfter,

    @Schema(description = "커서 (정렬 필드의 마지막 값)", example = "2024-01-02")
    String cursor,

    @Schema(description = "정렬 필드 (baseDate, marketPrice, closingPrice, highPrice, lowPrice, versus, fluctuationRate, tradingQuantity, tradingPrice, marketTotalAmount)", example = "baseDate")
    String sortField,

    @Schema(description = "정렬 방향 (asc, desc)", example = "desc")
    String sortDirection,

    @Schema(description = "페이지 크기 (1~100)", example = "10")
    @Min(1) @Max(100)
    Integer size

) {
  public IndexDataQueryCondition {
    if (sortField == null) sortField = "baseDate";
    if (sortDirection == null) sortDirection = "desc";
    if (size == null) size = 10;
  }
}