package com.sprint.mission.findex.domain.indexinfo.controller;

import com.sprint.mission.findex.domain.indexinfo.controller.api.IndexInfoApi;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoCreateRequest;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoUpdateRequest;
import com.sprint.mission.findex.domain.indexinfo.service.IndexInfoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
@RestController
public class IndexInfoController implements IndexInfoApi {

  private final IndexInfoService indexInfoService;

  @PostMapping
  public ResponseEntity<IndexInfoResponse> create(
      @RequestBody @Valid IndexInfoCreateRequest req) {
    IndexInfoResponse res = indexInfoService.createByUser(req);
    URI location = MvcUriComponentsBuilder.fromController(IndexInfoController.class)
        .path("/{id}")
        .buildAndExpand(res.id())
        .toUri();
    return ResponseEntity.status(HttpStatus.CREATED)
        .location(location)
        .body(res);
  }

  @PatchMapping(path = "{id}")
  public ResponseEntity<IndexInfoResponse> update(
      @PathVariable UUID id,
      @RequestBody @Valid IndexInfoUpdateRequest req) {
    IndexInfoResponse res = indexInfoService.updateByUser(id, req);
    return ResponseEntity.status(HttpStatus.OK)
        .body(res);
  }

  @DeleteMapping(path = "{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    indexInfoService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .build();
  }
}
