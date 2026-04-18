package com.sprint.mission.findex.domain.indexdata.repository.querydsl.impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataQueryCondition;
import com.sprint.mission.findex.domain.indexdata.entity.QIndexData;
import com.sprint.mission.findex.domain.indexdata.repository.querydsl.IndexDataCustomRepository;
import com.sprint.mission.findex.global.common.dto.CursorPageResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.Projections;
import com.sprint.mission.findex.domain.indexdata.dto.IndexDataResponse;

@Repository
@RequiredArgsConstructor
public class IndexDataCustomRepositoryImpl implements IndexDataCustomRepository {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int MAX_PAGE_SIZE = 100;

  private final JPAQueryFactory queryFactory;
  private final QIndexData indexData = QIndexData.indexData;

  @Override
  public CursorPageResponse<IndexDataResponse> findAll(IndexDataQueryCondition request) {
    int size = (request.size() != null && request.size() > 0)
        ? Math.min(request.size(), MAX_PAGE_SIZE)
        : DEFAULT_PAGE_SIZE;

    boolean asc = !"desc".equalsIgnoreCase(request.sortDirection());
    String sortField = request.sortField() != null ? request.sortField() : "baseDate";

    List<IndexDataResponse> content = queryFactory
        .select(Projections.constructor(IndexDataResponse.class,
            indexData.id,
            indexData.indexInfo.id,
            indexData.baseDate,
            indexData.sourceType,
            indexData.marketPrice,
            indexData.closingPrice,
            indexData.highPrice,
            indexData.lowPrice,
            indexData.versus,
            indexData.fluctuationRate,
            indexData.tradingQuantity,
            indexData.tradingPrice,
            indexData.marketTotalAmount
        ))
        .from(indexData)
        .where(
            eqIndexInfoId(request.indexInfoId()),
            goeStartDate(request.startDate()),
            loeEndDate(request.endDate()),
            cursorCondition(sortField, request.cursor(), request.idAfter(), asc)
        )
        .orderBy(
            sortOrder(sortField, asc),
            asc ? indexData.id.asc() : indexData.id.desc()
        )
        .limit(size + 1)
        .fetch();

    boolean hasNext = content.size() > size;
    List<IndexDataResponse> result = hasNext ? content.subList(0, size) : content;
    String nextCursor = hasNext ? extractCursor(sortField, result.get(result.size() - 1)) : null;
    UUID nextIdAfter = hasNext ? result.get(result.size() - 1).id() : null;

    Long totalElements = queryFactory
        .select(indexData.count())
        .from(indexData)
        .where(
            eqIndexInfoId(request.indexInfoId()),
            goeStartDate(request.startDate()),
            loeEndDate(request.endDate())
        )
        .fetchOne();

    return CursorPageResponse.of(result, nextCursor, nextIdAfter, size, totalElements, hasNext);
  }

  private BooleanExpression eqIndexInfoId(UUID indexInfoId) {
    return indexInfoId != null ? indexData.indexInfo.id.eq(indexInfoId) : null;
  }

  private BooleanExpression goeStartDate(LocalDate startDate) {
    return startDate != null ? indexData.baseDate.goe(startDate) : null;
  }

  private BooleanExpression loeEndDate(LocalDate endDate) {
    return endDate != null ? indexData.baseDate.loe(endDate) : null;
  }

  private BooleanExpression cursorCondition(
      String sortField, String cursor, UUID idAfter, boolean asc) {
    if (cursor == null || idAfter == null) return null;
    try {
      return switch (sortField) {
        case "marketPrice" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.marketPrice.gt(value).or(indexData.marketPrice.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.marketPrice.lt(value).or(indexData.marketPrice.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "closingPrice" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.closingPrice.gt(value).or(indexData.closingPrice.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.closingPrice.lt(value).or(indexData.closingPrice.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "highPrice" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.highPrice.gt(value).or(indexData.highPrice.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.highPrice.lt(value).or(indexData.highPrice.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "lowPrice" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.lowPrice.gt(value).or(indexData.lowPrice.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.lowPrice.lt(value).or(indexData.lowPrice.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "versus" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.versus.gt(value).or(indexData.versus.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.versus.lt(value).or(indexData.versus.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "fluctuationRate" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.fluctuationRate.gt(value).or(indexData.fluctuationRate.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.fluctuationRate.lt(value).or(indexData.fluctuationRate.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "tradingQuantity" -> {
          Long value = Long.parseLong(cursor);
          yield asc
              ? indexData.tradingQuantity.gt(value).or(indexData.tradingQuantity.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.tradingQuantity.lt(value).or(indexData.tradingQuantity.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "tradingPrice" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.tradingPrice.gt(value).or(indexData.tradingPrice.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.tradingPrice.lt(value).or(indexData.tradingPrice.eq(value).and(indexData.id.lt(idAfter)));
        }
        case "marketTotalAmount" -> {
          BigDecimal value = new BigDecimal(cursor);
          yield asc
              ? indexData.marketTotalAmount.gt(value).or(indexData.marketTotalAmount.eq(value).and(indexData.id.gt(idAfter)))
              : indexData.marketTotalAmount.lt(value).or(indexData.marketTotalAmount.eq(value).and(indexData.id.lt(idAfter)));
        }
        default -> {
          LocalDate date = LocalDate.parse(cursor);
          yield asc
              ? indexData.baseDate.gt(date).or(indexData.baseDate.eq(date).and(indexData.id.gt(idAfter)))
              : indexData.baseDate.lt(date).or(indexData.baseDate.eq(date).and(indexData.id.lt(idAfter)));
        }
      };
    } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
      throw new IllegalArgumentException("잘못된 cursor 값입니다: " + cursor);
    }
  }
  private OrderSpecifier<?> sortOrder(String sortField, boolean asc) {
    return switch (sortField) {
      case "marketPrice"      -> asc ? indexData.marketPrice.asc()      : indexData.marketPrice.desc();
      case "closingPrice"     -> asc ? indexData.closingPrice.asc()     : indexData.closingPrice.desc();
      case "highPrice"        -> asc ? indexData.highPrice.asc()        : indexData.highPrice.desc();
      case "lowPrice"         -> asc ? indexData.lowPrice.asc()         : indexData.lowPrice.desc();
      case "versus"           -> asc ? indexData.versus.asc()           : indexData.versus.desc();
      case "fluctuationRate"  -> asc ? indexData.fluctuationRate.asc()  : indexData.fluctuationRate.desc();
      case "tradingQuantity"  -> asc ? indexData.tradingQuantity.asc()  : indexData.tradingQuantity.desc();
      case "tradingPrice"     -> asc ? indexData.tradingPrice.asc()     : indexData.tradingPrice.desc();
      case "marketTotalAmount"-> asc ? indexData.marketTotalAmount.asc(): indexData.marketTotalAmount.desc();
      default                 -> asc ? indexData.baseDate.asc()         : indexData.baseDate.desc();
    };
  }

  private String extractCursor(String sortField, IndexDataResponse data) {
    return switch (sortField) {
      case "marketPrice"       -> data.marketPrice().toString();
      case "closingPrice"      -> data.closingPrice().toString();
      case "highPrice"         -> data.highPrice().toString();
      case "lowPrice"          -> data.lowPrice().toString();
      case "versus"            -> data.versus().toString();
      case "fluctuationRate"   -> data.fluctuationRate().toString();
      case "tradingQuantity"   -> data.tradingQuantity().toString();
      case "tradingPrice"      -> data.tradingPrice().toString();
      case "marketTotalAmount" -> data.marketTotalAmount().toString();
      default                  -> data.baseDate().toString();
    };
  }
}