package com.sprint.mission.findex.domain.indexdata.repository;

import com.sprint.mission.findex.domain.indexdata.entity.IndexData;
import com.sprint.mission.findex.domain.indexdata.repository.querydsl.IndexDataCustomRepository;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexDataRepository extends JpaRepository<IndexData, UUID>,
    IndexDataCustomRepository {

  boolean existsByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);

  Optional<IndexData> findByIndexInfoAndBaseDate(IndexInfo indexInfo, LocalDate baseDate);

  List<IndexData> findByIndexInfoIdAndBaseDateBetween(UUID indexInfoId, LocalDate from, LocalDate to);

  List<IndexData> findByBaseDateBetween(LocalDate from, LocalDate to);
}