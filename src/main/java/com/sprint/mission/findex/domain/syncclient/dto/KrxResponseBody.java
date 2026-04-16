package com.sprint.mission.findex.domain.syncclient.dto;

public record KrxResponseBody(
        Integer numOfRows,
        Integer pageNo,
        Integer totalCount,
        KrxItems items
) {
}