package com.sprint.mission.findex.domain.syncclient.dto;

public record KrxResponse(
        KrxResponseHeader header,
        KrxResponseBody body
) {
}