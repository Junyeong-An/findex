package com.sprint.mission.findex.domain.indexinfo.dto;

import com.sprint.mission.findex.domain.indexinfo.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "지수 정보 Response")
public record IndexInfoResponse(
    @Schema(description = "지수 정보 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName,

    @Schema(description = "채용 종목 수", example = "200")
    Integer employedItemsCount,

    @Schema(description = "기준 시점", example = "2000-01-01")
    LocalDate basePointInTime,

    @Schema(description = "기준 지수", example = "1000")
    BigDecimal baseIndex,

    @Schema(description = "출처 (사용자, Open API)", allowableValues = {"USER",
        "OPEN_API"}, example = "OPEN_API")
    SourceType sourceType,

    @Schema(description = "즐겨찾기 여부", example = "true")
    Boolean favorite
) {

}