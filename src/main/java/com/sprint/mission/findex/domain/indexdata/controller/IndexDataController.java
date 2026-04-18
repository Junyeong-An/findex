package com.sprint.mission.findex.domain.indexdata.controller;

import com.sprint.mission.findex.domain.indexdata.controller.api.IndexDataApi;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataCreateRequest;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataListRequest;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataUpdateRequest;
import com.sprint.mission.findex.domain.indexdata.service.IndexDataService;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController implements IndexDataApi {

  private final IndexDataService indexDataService;

  @PostMapping
  public ResponseEntity<IndexDataResponse> create(
      @Valid @RequestBody IndexDataCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(indexDataService.create(request));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexDataResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody IndexDataUpdateRequest request) {
    return ResponseEntity.ok(indexDataService.update(id, request));
  }

  @DeleteMapping("{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    indexDataService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<IndexDataResponse>> getList(
      @ModelAttribute IndexDataListRequest request) {
    return ResponseEntity.ok(indexDataService.getList(request));
  }


}
