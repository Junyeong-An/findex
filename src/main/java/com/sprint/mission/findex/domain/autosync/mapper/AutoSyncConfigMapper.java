package com.sprint.mission.findex.domain.autosync.mapper;

import com.sprint.mission.findex.domain.autosync.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosync.entity.AutoSyncConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {

  @Mapping(source = "indexInfo.id", target = "indexInfoId")
  @Mapping(source = "indexInfo.indexClassification", target = "indexClassification")
  @Mapping(source = "indexInfo.indexName", target = "indexName")
  AutoSyncConfigResponse toResponse(AutoSyncConfig config);
}
