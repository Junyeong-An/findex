package com.sprint.mission.findex.domain.autosync.repository;

import com.sprint.mission.findex.domain.autosync.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, UUID> {

  // M1(IndexInfoService)에서 IndexInfo 등록 시 중복 생성 방지용
  boolean existsByIndexInfo(IndexInfo indexInfo);

  // 설정 조회 (단건) - PATCH API, 배치에서 활용
  Optional<AutoSyncConfig> findByIndexInfo(IndexInfo indexInfo);

  // 배치(Spring Scheduler)에서 활성화된 지수 목록 전체 조회
  List<AutoSyncConfig> findAllByEnabledTrue();
}
