package com.sprint.mission.findex.domain.indexinfo.repository;

import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, UUID> {

  boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);
}
