package com.sprint.mission.findex.domain.indexdata.mapper;

import com.sprint.mission.findex.domain.indexdata.dto.IndexDataResponse;
import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.syncclient.dto.IndexDataApiResponse;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {

  @Mapping(target = "indexInfoId", expression = "java(indexData.getIndexInfo().getId())")
  IndexDataResponse toResponse(IndexData indexData);

  @Mapping(target = "indexInfo", source = "indexInfo")
  @Mapping(target = "sourceType", expression = "java(indexInfo.getSourceType())")
  @Mapping(target = "baseDate", source = "dto.basDt", dateFormat = "yyyyMMdd")
  @Mapping(target = "marketPrice", source = "dto.mkp")
  @Mapping(target = "closingPrice", source = "dto.clpr")
  @Mapping(target = "highPrice", source = "dto.hipr")
  @Mapping(target = "lowPrice", source = "dto.lopr")
  @Mapping(target = "versus", source = "dto.vs")
  @Mapping(target = "fluctuationRate", source = "dto.fltRt")
  @Mapping(target = "tradingQuantity", source = "dto.trqu")
  @Mapping(target = "tradingPrice", source = "dto.trPrc")
  @Mapping(target = "marketTotalAmount", source = "dto.lstgMrktTotAmt")
  IndexData toEntity(IndexDataApiResponse dto, IndexInfo indexInfo);

  default List<IndexData> toEntityList(List<IndexDataApiResponse> dtoList, IndexInfo indexInfo) {
    if (dtoList == null) return null;
    return dtoList.stream()
        .map(dto -> toEntity(dto, indexInfo))
        .toList();
  }
}