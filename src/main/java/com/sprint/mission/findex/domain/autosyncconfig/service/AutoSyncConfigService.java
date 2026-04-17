package com.sprint.mission.findex.domain.autosyncconfig.service;

import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.autosyncconfig.mapper.AutoSyncConfigMapper;
import com.sprint.mission.findex.domain.autosyncconfig.repository.AutoSyncConfigRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import com.sprint.mission.findex.global.common.mapper.CursorPageMapper;
import com.sprint.mission.findex.global.exception.ApiException;
import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoSyncConfigService {

  private static final int MIN_PAGE_SIZE = 1;
  private static final int MAX_PAGE_SIZE = 100;

  private final AutoSyncConfigMapper autoSyncConfigMapper;
  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final CursorPageMapper cursorPageMapper;

  @Transactional
  public AutoSyncConfigResponse updateEnabled(UUID id, AutoSyncConfigUpdateRequest request) {
    AutoSyncConfig config = autoSyncConfigRepository.findByIdWithIndexInfo(id)
        .orElseThrow(() -> new ApiException(ERROR.AUTO_SYNC_CONFIG_NOT_FOUND));
    config.updateEnabled(request.enabled());
    return autoSyncConfigMapper.toResponse(config);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<AutoSyncConfigResponse> findAll(
      UUID idAfter,
      UUID cursor,
      UUID indexInfoId,
      Boolean enabled,
      int size
  ) {
    // idAfter 우선, 없으면 cursor UUID를 사용
    UUID effectiveIdAfter = idAfter != null ? idAfter : cursor;
    int validatedSize = Math.max(MIN_PAGE_SIZE, Math.min(size, MAX_PAGE_SIZE));

    Page<AutoSyncConfig> page = autoSyncConfigRepository.findAllWithCursor(
        effectiveIdAfter,
        indexInfoId,
        enabled,
        PageRequest.of(0, validatedSize, Sort.by(Sort.Direction.ASC, "id"))
    );
    Page<AutoSyncConfigResponse> responsePage = page.map(autoSyncConfigMapper::toResponse);
    return cursorPageMapper.fromPage(responsePage, AutoSyncConfigResponse::id);
  }
}
