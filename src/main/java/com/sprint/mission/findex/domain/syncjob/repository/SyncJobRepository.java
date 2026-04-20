package com.sprint.mission.findex.domain.syncjob.repository;

import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import com.sprint.mission.findex.domain.syncjob.repository.querydsl.SyncJobCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID>, SyncJobCustomRepository {

  @Query("SELECT MAX(s.targetDate) FROM SyncJob s " +
      "WHERE s.indexInfo.id = :indexId " +
      "AND s.jobType = :jobType " +
      "AND s.result = com.sprint.mission.findex.domain.syncjob.entity.JobResult.SUCCESS")
  Optional<LocalDate> findLastSuccessDate(@Param("indexId") UUID indexId, @Param("jobType") JobType jobType);
}