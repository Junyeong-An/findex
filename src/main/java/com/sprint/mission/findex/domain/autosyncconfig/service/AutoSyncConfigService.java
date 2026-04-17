package com.sprint.mission.findex.domain.autosyncconfig.service;

import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosyncconfig.dto.AutoSyncConfigUpdateRequest;
import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.autosyncconfig.mapper.AutoSyncConfigMapper;
import com.sprint.mission.findex.domain.autosyncconfig.repository.AutoSyncConfigRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import com.sprint.mission.findex.global.exception.ApiException;
import com.sprint.mission.findex.global.exception.ApiException.ERROR;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoSyncConfigService {

  private static final int MIN_PAGE_SIZE = 1;
  private static final int MAX_PAGE_SIZE = 100;
  private static final String DEFAULT_SORT_FIELD = "indexInfo.indexName";
  private static final List<String> ALLOWED_SORT_FIELDS = List.of("indexInfo.indexName", "enabled");

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
  public CursorPageResponse<AutoSyncConfigResponse> findAll(
      UUID idAfter,
      String cursor,
      UUID indexInfoId,
      Boolean enabled,
      String sortField,
      String sortDirection,
      int size
  ) {
    int validatedSize = Math.max(MIN_PAGE_SIZE, Math.min(size, MAX_PAGE_SIZE));
    String effectiveSortField = (sortField == null || sortField.isBlank()) ? DEFAULT_SORT_FIELD : sortField;
    if (!ALLOWED_SORT_FIELDS.contains(effectiveSortField)) {
      throw new ApiException(ERROR.INVALID_SORT_FIELD);
    }
    boolean asc = !"desc".equalsIgnoreCase(sortDirection);

    List<AutoSyncConfig> results = autoSyncConfigRepository.findAllWithCursor(
        cursor,
        idAfter,
        indexInfoId,
        enabled,
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

    long totalElements = autoSyncConfigRepository.countWithFilter(indexInfoId, enabled);

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
