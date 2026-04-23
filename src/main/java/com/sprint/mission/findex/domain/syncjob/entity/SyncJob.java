package com.sprint.mission.findex.domain.syncjob.entity;

import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "sync_job",
    // PostgreSQL에서는 부분 인덱스로 생성: WHERE (result = 'SUCCESS')
    indexes = @Index(name = "idx_sync_job_last_success", columnList = "index_info_id, job_type, target_date DESC")
)
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