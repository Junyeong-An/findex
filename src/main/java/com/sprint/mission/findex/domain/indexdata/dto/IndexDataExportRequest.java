package com.sprint.mission.findex.domain.indexdata.dto;

import java.time.LocalDate;
import java.util.UUID;

public record IndexDataExportRequest(
    UUID indexInfoId,
    LocalDate startDate,
    LocalDate endDate,
    String sortField,
    String sortDirection
) {
  public IndexDataExportRequest {
    if (sortField == null) sortField = "baseDate";
    if (sortDirection == null) sortDirection = "desc";
  }
}