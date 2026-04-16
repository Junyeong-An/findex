package com.sprint.mission.findex.domain.autosync.service;

import com.sprint.mission.findex.domain.autosync.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosync.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.findex.domain.autosync.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.autosync.repository.AutoSyncConfigRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import com.sprint.mission.findex.global.common.mapper.CursorPageMapper;
import com.sprint.mission.findex.global.exception.ApiException;
import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoSyncConfigService {

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final CursorPageMapper cursorPageMapper;

  @Transactional
  public AutoSyncConfigResponse updateEnabled(UUID id, AutoSyncConfigUpdateRequest request) {
    AutoSyncConfig config = autoSyncConfigRepository.findByIdWithIndexInfo(id)
        .orElseThrow(() -> new ApiException(ERROR.AUTO_SYNC_CONFIG_NOT_FOUND));
    config.updateEnabled(request.enabled());
    return AutoSyncConfigResponse.from(config);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<AutoSyncConfigResponse> findAll(
      UUID idAfter,
      String cursor,
      UUID indexInfoId,
      Boolean enabled,
      String sortField,
      String sortDirection,
      int size
  ) {
    // idAfter 우선, 없으면 cursor(String)를 UUID로 파싱해서 사용
    UUID effectiveIdAfter = idAfter;
    if (effectiveIdAfter == null && cursor != null) {
      try {
        effectiveIdAfter = UUID.fromString(cursor);
      } catch (IllegalArgumentException ignored) {
        // 유효하지 않은 cursor 값이면 무시
      }
    }

    Sort sort = Sort.by(
        "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
        sortField
    );

    Slice<AutoSyncConfig> slice = autoSyncConfigRepository.findAllWithCursor(
        effectiveIdAfter, indexInfoId, enabled, PageRequest.of(0, size, sort)
    );
    Slice<AutoSyncConfigResponse> responsePage = slice.map(AutoSyncConfigResponse::from);
    return cursorPageMapper.fromSlice(responsePage, AutoSyncConfigResponse::id);
  }
}
