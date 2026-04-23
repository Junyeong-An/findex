package com.sprint.mission.findex.domain.dashboard.controller;

import com.sprint.mission.findex.domain.dashboard.controller.api.DashboardApi;
import com.sprint.mission.findex.domain.dashboard.dto.RankedIndexPerformanceQueryCondition;
import com.sprint.mission.findex.domain.dashboard.dto.RankedIndexPerformanceResponse;
import com.sprint.mission.findex.domain.dashboard.service.DashboardService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-data")
public class DashboardController implements DashboardApi {

  private final DashboardService dashboardService;

  @GetMapping(path = "performance/rank")
  public ResponseEntity<List<RankedIndexPerformanceResponse>> getIndexPerformanceRank(
      @ParameterObject @ModelAttribute @Valid RankedIndexPerformanceQueryCondition condition) {
    List<RankedIndexPerformanceResponse> res = dashboardService.getIndexPerformanceRank(
        condition);

    return ResponseEntity.status(HttpStatus.OK)
        .body(res);
  }
}

