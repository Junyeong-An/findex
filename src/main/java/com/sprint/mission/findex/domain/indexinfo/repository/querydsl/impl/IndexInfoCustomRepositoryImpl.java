package com.sprint.mission.findex.domain.indexinfo.repository.querydsl.impl;

import static com.sprint.mission.findex.domain.indexinfo.entity.QIndexInfo.indexInfo;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.util.StringUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoQueryCondition;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoResponse;
import com.sprint.mission.findex.domain.indexinfo.dto.IndexInfoSummaryResponse;
import com.sprint.mission.findex.domain.indexinfo.repository.querydsl.IndexInfoCustomRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class IndexInfoCustomRepositoryImpl implements IndexInfoCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<IndexInfoSummaryResponse> findIndexInfoSummaries() {
    return queryFactory
        .select(Projections.constructor(
            IndexInfoSummaryResponse.class,
            indexInfo.id,
            indexInfo.indexClassification,
            indexInfo.indexName
        ))
        .from(indexInfo)
        .fetch();
  }

  @Override
  public CursorPageResponse<IndexInfoResponse> findIndexInfos(IndexInfoQueryCondition condition) {
    List<IndexInfoResponse> content = queryFactory
        .select(Projections.constructor(
            IndexInfoResponse.class,
            indexInfo.id,
            indexInfo.indexClassification,
            indexInfo.indexName,
            indexInfo.employedItemsCount,
            indexInfo.basePointInTime,
            indexInfo.baseIndex,
            indexInfo.sourceType,
            indexInfo.favorite
        ))
        .from(indexInfo)
        .where(
            likeIndexClassification(condition.indexClassification()),
            likeIndexName(condition.indexName()),
            eqFavorite(condition.favorite()),
            cursorCondition(condition)
        )
        .orderBy(
            buildOrderSpecifier(condition.sortField(), condition.sortDirection()),
            buildIdOrderSpecifier(condition.sortDirection())
        )
        .limit(condition.size() + 1)
        .fetch();

    long totalElements = Optional.ofNullable(
        queryFactory
            .select(indexInfo.count())
            .from(indexInfo)
            .where(
                likeIndexClassification(condition.indexClassification()),
                likeIndexName(condition.indexName()),
                eqFavorite(condition.favorite())
            )
            .fetchOne()
    ).orElse(0L);

    boolean hasNext = content.size() > condition.size();
    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext) {
      nextCursor = extractCursor(condition.sortField(), content.get(condition.size() - 1));
      nextIdAfter = content.get(condition.size() - 1).id();
      content = content.subList(0, condition.size());
    }

    return CursorPageResponse.of(
        content,
        nextCursor,
        nextIdAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  private BooleanExpression likeIndexClassification(String indexClassification) {
    return StringUtils.isNullOrEmpty(indexClassification) ? null
        : indexInfo.indexClassification.containsIgnoreCase(indexClassification);
  }

  private BooleanExpression likeIndexName(String indexName) {
    return StringUtils.isNullOrEmpty(indexName) ? null
        : indexInfo.indexName.containsIgnoreCase(indexName);
  }

  private BooleanExpression eqFavorite(Boolean favorite) {
    return favorite != null ? indexInfo.favorite.eq(favorite) : null;
  }

  private BooleanExpression cursorCondition(IndexInfoQueryCondition condition) {
    String cursor = condition.cursor();
    UUID idAfter = condition.idAfter();
    boolean isAsc = !"desc".equalsIgnoreCase(condition.sortDirection());
    if (cursor == null) {
      return null;
    }
    return switch (condition.sortField()) {
      case "indexName" -> buildCursorExpression(indexInfo.indexName, cursor, idAfter, isAsc);
      case "employedItemsCount" ->
          buildCursorExpression(indexInfo.employedItemsCount, Integer.valueOf(cursor), idAfter,
              isAsc);
      default -> buildCursorExpression(indexInfo.indexClassification, cursor, idAfter, isAsc);
    };
  }

  private <T extends Comparable<T>> BooleanExpression buildCursorExpression(
      ComparableExpression<T> field, T cursorValue, UUID idAfter, boolean isAsc) {
    return isAsc
        ? field.gt(cursorValue).or(field.eq(cursorValue).and(indexInfo.id.gt(idAfter)))
        : field.lt(cursorValue).or(field.eq(cursorValue).and(indexInfo.id.lt(idAfter)));
  }

  private BooleanExpression buildCursorExpression(
      NumberExpression<Integer> field, Integer cursorValue, UUID idAfter, boolean isAsc) {
    return isAsc
        ? field.gt(cursorValue).or(field.eq(cursorValue).and(indexInfo.id.gt(idAfter)))
        : field.lt(cursorValue).or(field.eq(cursorValue).and(indexInfo.id.lt(idAfter)));
  }

  private OrderSpecifier<?> buildOrderSpecifier(String sortField, String sortDirection) {
    Order direction = "desc".equalsIgnoreCase(sortDirection) ? Order.DESC : Order.ASC;
    return switch (sortField) {
      case "indexName" -> new OrderSpecifier<>(direction, indexInfo.indexName);
      case "employedItemsCount" -> new OrderSpecifier<>(direction, indexInfo.employedItemsCount);
      default -> new OrderSpecifier<>(direction, indexInfo.indexClassification);
    };
  }

  private OrderSpecifier<?> buildIdOrderSpecifier(String sortDirection) {
    Order direction = "desc".equalsIgnoreCase(sortDirection) ? Order.DESC : Order.ASC;
    return new OrderSpecifier<>(direction, indexInfo.id);
  }

  private String extractCursor(String sortField, IndexInfoResponse response) {
    return switch (sortField) {
      case "indexName" -> response.indexName();
      case "employedItemsCount" -> String.valueOf(response.employedItemsCount());
      default -> response.indexClassification();
    };
  }
}