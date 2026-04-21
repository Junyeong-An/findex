package com.sprint.mission.findex.domain.autosyncconfig.service;

import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncQueryCondition;
import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.autosyncconfig.mapper.AutoSyncConfigMapper;
import com.sprint.mission.findex.domain.autosyncconfig.repository.AutoSyncConfigRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import com.sprint.mission.findex.global.exception.ApiException;
import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoSyncConfigService {

  private static final int MIN_PAGE_SIZE = 1;
  private static final int MAX_PAGE_SIZE = 100;
  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("indexInfo.indexName", "enabled");

  private final AutoSyncConfigMapper autoSyncConfigMapper;
  private final AutoSyncConfigRepository autoSyncConfigRepository;

  @Transactional
  public AutoSyncConfigResponse updateEnabled(UUID id, AutoSyncConfigUpdateRequest request) {
    AutoSyncConfig config = autoSyncConfigRepository.findByIdWithIndexInfo(id)
        .orElseThrow(() -> new ApiException(ERROR.AUTO_SYNC_CONFIG_NOT_FOUND));
    config.updateEnabled(request.enabled());
    return autoSyncConfigMapper.toResponse(config);
  }

  @Transactional(readOnly = true)
  public CursorPageResponse<AutoSyncConfigResponse> findAll(AutoSyncQueryCondition condition) {
    int validatedSize = Math.max(MIN_PAGE_SIZE, Math.min(condition.size(), MAX_PAGE_SIZE));
    String effectiveSortField = ALLOWED_SORT_FIELDS.contains(condition.sortField())
        ? condition.sortField() : "indexInfo.indexName";
    boolean asc = !"desc".equalsIgnoreCase(condition.sortDirection());

    List<AutoSyncConfig> results = autoSyncConfigRepository.findAllWithCursor(
        condition.cursor(),
        condition.idAfter(),
        condition.indexInfoId(),
        condition.enabled(),
        effectiveSortField,
        asc,
        validatedSize + 1
    );

    boolean hasNext = results.size() > validatedSize;

    List<AutoSyncConfigResponse> content = results.stream()
        .limit(validatedSize)
        .map(autoSyncConfigMapper::toResponse)
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext) {
      AutoSyncConfigResponse last = content.get(content.size() - 1);
      nextIdAfter = last.id();
      nextCursor = extractCursor(last, effectiveSortField);
    }

    long totalElements = autoSyncConfigRepository.countWithFilter(
        condition.indexInfoId(), condition.enabled());

    return CursorPageResponse.of(
        content,
        nextCursor,
        nextIdAfter,
        validatedSize,
        totalElements,
        hasNext
    );
  }

  private String extractCursor(AutoSyncConfigResponse last, String sortField) {
    return switch (sortField) {
      case "indexInfo.indexName" -> last.indexName();
      case "enabled" -> String.valueOf(last.enabled());
      default -> last.id().toString();
    };
  }
}
