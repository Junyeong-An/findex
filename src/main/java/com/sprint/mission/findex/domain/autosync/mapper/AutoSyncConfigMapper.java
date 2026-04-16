package com.sprint.mission.findex.domain.autosync.mapper;

import com.sprint.mission.findex.domain.autosync.dto.AutoSyncConfigResponse;
import com.sprint.mission.findex.domain.autosync.entity.AutoSyncConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {

  @Mapping(target = "indexInfoId", expression = "java(autoSyncConfig.getIndexInfo().getId())")
  @Mapping(target = "indexClassification", expression = "java(autoSyncConfig.getIndexInfo().getIndexClassification())")
  @Mapping(target = "indexName", expression = "java(autoSyncConfig.getIndexInfo().getIndexName())")
  AutoSyncConfigResponse toResponse(AutoSyncConfig autoSyncConfig);
}
