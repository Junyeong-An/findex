package com.sprint.mission.findex.domain.autosyncconfig.repository;

import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, UUID>,
    AutoSyncConfigRepositoryCustom {

  // M1(IndexInfoService)에서 IndexInfo 등록 시 중복 생성 방지용
  boolean existsByIndexInfo(IndexInfo indexInfo);

  // 설정 조회 (단건) - PATCH API, 배치에서 활용
  Optional<AutoSyncConfig> findByIndexInfo(IndexInfo indexInfo);

  // PATCH API - IndexInfo 함께 로딩 (N+1 방지)
  @Query("SELECT a FROM AutoSyncConfig a JOIN FETCH a.indexInfo WHERE a.id = :id")
  Optional<AutoSyncConfig> findByIdWithIndexInfo(@Param("id") UUID id);

  // 배치(Spring Scheduler) 등에서 enabled 조건에 맞는 지수 목록 전체 조회
  @Query("SELECT a FROM AutoSyncConfig a JOIN FETCH a.indexInfo WHERE a.enabled = :enabled")
  List<AutoSyncConfig> findAllByEnabled(@Param("enabled") boolean enabled);
}
