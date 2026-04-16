package com.sprint.mission.findex.domain.indexinfo.mapper;

import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IndexInfoMapper {

  IndexInfoResponse toResponse(IndexInfo indexInfo);
}
