package com.sprint.mission.findex.domain.syncclient.dto;

import java.math.BigDecimal;

public record IndexDataApiResponse(
        String basDt,
        String idxNm,
        String idxCsf,
        Integer epyItmsCnt,
        BigDecimal clpr,
        BigDecimal vs,
        BigDecimal fltRt,
        BigDecimal mkp,
        BigDecimal hipr,
        BigDecimal lopr,
        Long trqu,
        BigDecimal trPrc,
        BigDecimal lstgMrktTotAmt,
        String basPntm,
        BigDecimal basIdx
) {
}