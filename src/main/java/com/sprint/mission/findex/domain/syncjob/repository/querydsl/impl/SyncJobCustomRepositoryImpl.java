package com.sprint.mission.findex.domain.syncjob.repository.querydsl.impl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobResponse;
import com.sprint.mission.findex.domain.syncjob.dto.SyncJobQueryCondition;
import com.sprint.mission.findex.domain.syncjob.entity.JobResult;
import com.sprint.mission.findex.domain.syncjob.entity.JobType;
import com.sprint.mission.findex.domain.syncjob.entity.SyncJob;
import com.sprint.mission.findex.domain.syncjob.repository.querydsl.SyncJobCustomRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.sprint.mission.findex.domain.indexinfo.entity.QIndexInfo.indexInfo;
import static com.sprint.mission.findex.domain.syncjob.entity.QSyncJob.syncJob;

@Repository
@RequiredArgsConstructor
public class SyncJobCustomRepositoryImpl implements SyncJobCustomRepository {

  private final JPAQueryFactory queryFactory;

  private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

  @Override
  public CursorPageResponse<SyncJobResponse> searchSyncJobPage(
      SyncJobQueryCondition condition, String cursor, UUID idAfter,
      String sortField, String sortDirection, int size) {

    String activeSortField = (sortField != null && !sortField.isBlank()) ? sortField : "jobTime";
    boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

    List<SyncJob> syncJobs = queryFactory
        .selectFrom(syncJob)
        .leftJoin(syncJob.indexInfo, indexInfo).fetchJoin()
        .where(
            eqJobType(condition.jobType()),
            eqIndexInfoId(condition.indexInfoId()),
            eqStatus(condition.status()),
            goeBaseDateFrom(condition.baseDateFrom()),
            loeBaseDateTo(condition.baseDateTo()),
            containsWorker(condition.worker()),
            goeJobTimeFrom(condition.jobTimeFrom()),
            loeJobTimeTo(condition.jobTimeTo()),
            getCursorCondition(cursor, idAfter, activeSortField, isAsc)
        )
        .orderBy(getOrderSpecifiers(activeSortField, isAsc))
        .limit(size + 1L)
        .fetch();

    Long totalElements = queryFactory
        .select(syncJob.count())
        .from(syncJob)
        .where(
            eqJobType(condition.jobType()),
            eqIndexInfoId(condition.indexInfoId()),
            eqStatus(condition.status()),
            goeBaseDateFrom(condition.baseDateFrom()),
            loeBaseDateTo(condition.baseDateTo()),
            containsWorker(condition.worker()),
            goeJobTimeFrom(condition.jobTimeFrom()),
            loeJobTimeTo(condition.jobTimeTo())
        )
        .fetchOne();

    boolean hasNext = syncJobs.size() > size;

    List<SyncJobResponse> content = syncJobs.stream()
        .limit(size)
        .map(SyncJobResponse::from)
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;

    if (!content.isEmpty()) {
      SyncJobResponse lastElement = content.get(content.size() - 1);
      nextIdAfter = lastElement.id();

      if ("targetDate".equals(activeSortField)) {
        nextCursor = lastElement.targetDate().toString();
      } else {
        nextCursor = lastElement.jobTime().toString();
      }
    }

    return CursorPageResponse.of(
        content,
        nextCursor,
        nextIdAfter,
        size,
        totalElements,
        hasNext
    );
  }

  private BooleanExpression getCursorCondition(String cursor, UUID idAfter, String sortField, boolean isAsc) {
    if (cursor == null || cursor.isBlank() || idAfter == null) return null;

    if ("targetDate".equals(sortField)) {
      LocalDate targetDateCursor;
      try {
        targetDateCursor = LocalDate.parse(cursor);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("잘못된 커서 형식입니다: " + cursor, e);
      }

      if (isAsc) {
        return syncJob.targetDate.gt(targetDateCursor)
            .or(syncJob.targetDate.eq(targetDateCursor).and(syncJob.id.gt(idAfter)));
      } else {
        return syncJob.targetDate.lt(targetDateCursor)
            .or(syncJob.targetDate.eq(targetDateCursor).and(syncJob.id.lt(idAfter)));
      }
    } else {
      Instant jobTimeCursor;
      try {
        jobTimeCursor = Instant.parse(cursor);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("잘못된 커서 형식입니다: %s".formatted(cursor), e);
      }

      if (isAsc) {
        return syncJob.jobTime.gt(jobTimeCursor)
            .or(syncJob.jobTime.eq(jobTimeCursor).and(syncJob.id.gt(idAfter)));
      } else {
        return syncJob.jobTime.lt(jobTimeCursor)
            .or(syncJob.jobTime.eq(jobTimeCursor).and(syncJob.id.lt(idAfter)));
      }
    }
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(String sortField, boolean isAsc) {
    Order direction = isAsc ? Order.ASC : Order.DESC;

    OrderSpecifier<?> primaryOrder = "targetDate".equals(sortField)
        ? new OrderSpecifier<>(direction, syncJob.targetDate)
        : new OrderSpecifier<>(direction, syncJob.jobTime);

    OrderSpecifier<?> secondaryOrder = new OrderSpecifier<>(direction, syncJob.id);

    return new OrderSpecifier[]{primaryOrder, secondaryOrder};
  }

  private BooleanExpression eqJobType(JobType jobType) { return jobType != null ? syncJob.jobType.eq(jobType) : null; }
  private BooleanExpression eqIndexInfoId(UUID indexInfoId) { return indexInfoId != null ? syncJob.indexInfo.id.eq(indexInfoId) : null; }
  private BooleanExpression eqStatus(JobResult status) { return status != null ? syncJob.result.eq(status) : null; }
  private BooleanExpression goeBaseDateFrom(LocalDate baseDateFrom) { return baseDateFrom != null ? syncJob.targetDate.goe(baseDateFrom) : null; }
  private BooleanExpression loeBaseDateTo(LocalDate baseDateTo) { return baseDateTo != null ? syncJob.targetDate.loe(baseDateTo) : null; }
  private BooleanExpression containsWorker(String worker) { return worker != null && !worker.isBlank() ? syncJob.worker.contains(worker) : null; }
  private BooleanExpression goeJobTimeFrom(Instant jobTimeFrom) { return jobTimeFrom != null ? syncJob.jobTime.goe(jobTimeFrom) : null; }
  private BooleanExpression loeJobTimeTo(Instant jobTimeTo) { return jobTimeTo != null ? syncJob.jobTime.loe(jobTimeTo) : null; }

  private BooleanExpression goeJobTimeFrom(LocalDate jobTimeFrom) {
    if (jobTimeFrom == null) return null;
    Instant fromInstant = jobTimeFrom.atStartOfDay(KST_ZONE).toInstant();
    return syncJob.jobTime.goe(fromInstant);
  }

  private BooleanExpression loeJobTimeTo(LocalDate jobTimeTo) {
    if (jobTimeTo == null) return null;
    Instant nextDayStart = jobTimeTo.plusDays(1).atStartOfDay(KST_ZONE).toInstant();
    return syncJob.jobTime.lt(nextDayStart);
  }
}