package com.sprint.mission.findex.domain.indexdata.dto;

import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo.SourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IndexDataCreateRequest (

  @NotNull
  UUID indexInfoId,

  @NotNull
  LocalDate baseDate,

  SourceType sourceType,

  @NotNull
  @PositiveOrZero
  BigDecimal marketPrice,

  @NotNull
  @PositiveOrZero
  BigDecimal closingPrice,

  @NotNull
  @PositiveOrZero
  BigDecimal highPrice,

  @NotNull
  @PositiveOrZero
  BigDecimal lowPrice,

  @NotNull
  BigDecimal versus,

  @NotNull
  BigDecimal fluctuationRate,

  @NotNull
  @PositiveOrZero
  Long tradingQuantity,

  @NotNull
  @PositiveOrZero
  BigDecimal tradingPrice,

  @NotNull
  @PositiveOrZero
  BigDecimal marketTotalAmount
) {}
