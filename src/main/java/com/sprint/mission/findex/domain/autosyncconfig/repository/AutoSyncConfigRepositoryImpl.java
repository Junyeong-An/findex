package com.sprint.mission.findex.domain.autosyncconfig.repository;

import static com.sprint.mission.findex.domain.autosyncconfig.entity.QAutoSyncConfig.autoSyncConfig;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.findex.domain.autosyncconfig.entity.AutoSyncConfig;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSyncConfigRepositoryImpl implements AutoSyncConfigRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<AutoSyncConfig> findAllWithCursor(
      String cursor,
      UUID idAfter,
      UUID indexInfoId,
      Boolean enabled,
      String sortField,
      boolean asc,
      int size
  ) {
    return queryFactory
        .selectFrom(autoSyncConfig)
        .join(autoSyncConfig.indexInfo).fetchJoin()
        .where(
            cursorCondition(sortField, cursor, idAfter, asc),
            indexInfoIdEq(indexInfoId),
            enabledEq(enabled)
        )
        .orderBy(buildOrderSpecifiers(sortField, asc))
        .limit(size)
        .fetch();
  }

  @Override
  public long countWithFilter(UUID indexInfoId, Boolean enabled) {
    Long count = queryFactory
        .select(autoSyncConfig.count())
        .from(autoSyncConfig)
        .where(
            indexInfoIdEq(indexInfoId),
            enabledEq(enabled)
        )
        .fetchOne();
    return count != null ? count : 0L;
  }

  private BooleanExpression cursorCondition(String sortField, String cursor, UUID idAfter, boolean asc) {
    if (cursor == null || idAfter == null) {
      return null;
    }
    return switch (sortField) {
      case "indexInfo.indexName" -> asc
          ? autoSyncConfig.indexInfo.indexName.gt(cursor)
              .or(autoSyncConfig.indexInfo.indexName.eq(cursor).and(autoSyncConfig.id.gt(idAfter)))
          : autoSyncConfig.indexInfo.indexName.lt(cursor)
              .or(autoSyncConfig.indexInfo.indexName.eq(cursor).and(autoSyncConfig.id.gt(idAfter)));
      case "enabled" -> buildEnabledCursorCondition(cursor, idAfter, asc);
      default -> asc
          ? autoSyncConfig.id.gt(idAfter)
          : autoSyncConfig.id.lt(idAfter);
    };
  }

  private BooleanExpression buildEnabledCursorCondition(String cursor, UUID idAfter, boolean asc) {
    boolean cursorBool = Boolean.parseBoolean(cursor);
    BooleanExpression sameField = autoSyncConfig.enabled.eq(cursorBool);
    if (asc) {
      // false(0) < true(1): cursor가 false면 enabled=true인 행도 포함
      BooleanExpression greaterThan = cursorBool ? null : autoSyncConfig.enabled.isTrue();
      BooleanExpression sameWithTiebreak = sameField.and(autoSyncConfig.id.gt(idAfter));
      return greaterThan != null ? greaterThan.or(sameWithTiebreak) : sameWithTiebreak;
    } else {
      // cursor가 true면 enabled=false인 행도 포함
      BooleanExpression lessThan = cursorBool ? autoSyncConfig.enabled.isFalse() : null;
      BooleanExpression sameWithTiebreak = sameField.and(autoSyncConfig.id.gt(idAfter));
      return lessThan != null ? lessThan.or(sameWithTiebreak) : sameWithTiebreak;
    }
  }

  private OrderSpecifier<?>[] buildOrderSpecifiers(String sortField, boolean asc) {
    Order order = asc ? Order.ASC : Order.DESC;
    return switch (sortField) {
      case "indexInfo.indexName" -> new OrderSpecifier<?>[]{
          new OrderSpecifier<>(order, autoSyncConfig.indexInfo.indexName),
          new OrderSpecifier<>(Order.ASC, autoSyncConfig.id)
      };
      case "enabled" -> new OrderSpecifier<?>[]{
          new OrderSpecifier<>(order, autoSyncConfig.enabled),
          new OrderSpecifier<>(Order.ASC, autoSyncConfig.id)
      };
      default -> new OrderSpecifier<?>[]{
          new OrderSpecifier<>(Order.ASC, autoSyncConfig.id)
      };
    };
  }

  private BooleanExpression indexInfoIdEq(UUID indexInfoId) {
    return indexInfoId != null ? autoSyncConfig.indexInfo.id.eq(indexInfoId) : null;
  }

  private BooleanExpression enabledEq(Boolean enabled) {
    return enabled != null ? autoSyncConfig.enabled.eq(enabled) : null;
  }
}
