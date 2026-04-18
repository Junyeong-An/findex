package com.sprint.mission.findex.domain.indexinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "지수 정보 요약 Response")
public record IndexInfoSummaryResponse(
    @Schema(description = "지수 정보 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "지수 분류명", example = "KOSPI시리즈")
    String indexClassification,

    @Schema(description = "지수명", example = "IT 서비스")
    String indexName
) {

}