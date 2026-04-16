package com.sprint.mission.findex.domain.indexdata.mapper;

import com.sprint.mission.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {

  @Mapping(target = "indexInfoId", expression = "java(indexData.getIndexInfo().getId())")
  IndexDataResponse toResponse(IndexData indexData);
}