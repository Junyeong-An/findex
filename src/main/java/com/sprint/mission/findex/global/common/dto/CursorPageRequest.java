package com.sprint.mission.findex.global.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.util.UUID;

@Schema(description = "커서 페이지네이션 요청")
public record CursorPageRequest(

    @Schema(description = "이전 페이지 마지막 ID (tiebreaker)")
    UUID idAfter,

    @Schema(description = "커서 값 (정렬 필드 기준 마지막 값)")
    String cursor,

    @Schema(description = "정렬 필드")
    String sortField,

    @Schema(description = "정렬 방향 (asc, desc)")
    String sortDirection,

    @Min(1)
    @Schema(description = "페이지 크기", example = "10")
    Integer size
) {}
