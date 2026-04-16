package com.sprint.mission.findex.domain.syncjob.entity;

import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sync_job")
public class SyncJob extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false, length = 20)
  private JobType jobType; //INDEX_INFO / INDEX_DATA

  @Column(name = "target_date")
  private LocalDate targetDate; //연동 대상 날짜

  @Column(name = "worker", nullable = false, length = 100)
  private String worker; //요청자 IP 또는 "system"

  @Column(name = "job_time", nullable = false)
  private Instant jobTime; //작업 실행 시각

  @Enumerated(EnumType.STRING)
  @Column(name = "result", nullable = false, length = 10)
  private JobResult result; //SUCCESS / FAILED

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage; //FAILED 시 예외 메시지

  @Builder
  public SyncJob(IndexInfo indexInfo, JobType jobType, LocalDate targetDate,
      String worker, JobResult result, String errorMessage) {
    this.indexInfo = indexInfo;
    this.jobType = jobType;
    this.targetDate = targetDate;
    this.worker = worker;
    this.result = result;
    this.errorMessage = errorMessage;
    this.jobTime = Instant.now();
  }
}