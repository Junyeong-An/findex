# 정렬 필드 Switch 패턴

Repository 구현체에서 switch로 정렬/커서 조건을 직접 분기하는 방식.
가장 흔하게 사용되는 접근법.

---

## 구현

```java
// IndexInfoCustomRepositoryImpl.java
import static com.sprint.mission.findex.domain.indexinfo.entity.QIndexInfo.indexInfo;

public CursorPageResponse<IndexInfoResponse> findAll(IndexInfoSearchCond cond) {
    boolean asc = "asc".equalsIgnoreCase(cond.sortDirection());

    List<IndexInfoResponse> content = queryFactory
        .select(/* Projections */)
        .from(indexInfo)
        .where(
            likeIndexClassification(cond.indexClassification()),
            likeIndexName(cond.indexName()),
            eqFavorite(cond.favorite()),
            cursorCondition(cond.sortField(), cond.cursor(), cond.idAfter(), asc)
        )
        .orderBy(sortOrder(cond.sortField(), asc), indexInfo.id.asc())
        .limit(cond.size() + 1)
        .fetch();

    boolean hasNext = content.size() > cond.size();
    List<IndexInfoResponse> result = hasNext ? content.subList(0, cond.size()) : content;

    String nextCursor = hasNext ? extractCursor(cond.sortField(), result.get(result.size() - 1)) : null;
    UUID nextIdAfter = hasNext ? result.get(result.size() - 1).id() : null;

    return new CursorPageResponse<>(result, nextCursor, nextIdAfter, cond.size(), null, hasNext);
}

// 커서 조건
private BooleanExpression cursorCondition(
        String sortField, String cursor, UUID idAfter, boolean asc) {
    if (cursor == null || idAfter == null) return null;

    return switch (sortField) {
        case "indexName" -> asc
            ? indexInfo.indexName.gt(cursor)
                .or(indexInfo.indexName.eq(cursor).and(indexInfo.id.gt(idAfter)))
            : indexInfo.indexName.lt(cursor)
                .or(indexInfo.indexName.eq(cursor).and(indexInfo.id.lt(idAfter)));

        case "employedItemsCount" -> {
            Integer cursorValue = Integer.parseInt(cursor);
            yield asc
                ? indexInfo.employedItemsCount.gt(cursorValue)
                    .or(indexInfo.employedItemsCount.eq(cursorValue).and(indexInfo.id.gt(idAfter)))
                : indexInfo.employedItemsCount.lt(cursorValue)
                    .or(indexInfo.employedItemsCount.eq(cursorValue).and(indexInfo.id.lt(idAfter)));
        }

        default -> asc  // indexClassification (기본값)
            ? indexInfo.indexClassification.gt(cursor)
                .or(indexInfo.indexClassification.eq(cursor).and(indexInfo.id.gt(idAfter)))
            : indexInfo.indexClassification.lt(cursor)
                .or(indexInfo.indexClassification.eq(cursor).and(indexInfo.id.lt(idAfter)));
    };
}

// 정렬 순서
private OrderSpecifier<?> sortOrder(String sortField, boolean asc) {
    return switch (sortField) {
        case "indexName"           -> asc ? indexInfo.indexName.asc()           : indexInfo.indexName.desc();
        case "employedItemsCount"  -> asc ? indexInfo.employedItemsCount.asc()  : indexInfo.employedItemsCount.desc();
        default                    -> asc ? indexInfo.indexClassification.asc() : indexInfo.indexClassification.desc();
    };
}

// 다음 커서 추출
private String extractCursor(String sortField, IndexInfoResponse dto) {
    return switch (sortField) {
        case "indexName"          -> dto.indexName();
        case "employedItemsCount" -> String.valueOf(dto.employedItemsCount());
        default                   -> dto.indexClassification();
    };
}
```

---

## 특징

- 로직이 Repository 구현체에 집중되어 흐름을 한눈에 파악 가능
- 정렬 필드 추가 시 switch 3곳(`cursorCondition`, `sortOrder`, `extractCursor`) 수정 필요
- 별도 클래스/파일 없이 단순하게 유지 가능

---

## enum 패턴과 비교

| | Switch 패턴 | Enum 패턴 |
|---|---|---|
| 가독성 | 흐름이 한 파일에 집중 | 로직이 분산되어 추적 필요 |
| 확장성 | switch 3곳 수정 | enum 상수 1개 추가 |
| 복잡도 | 낮음 | 높음 |
| 적합한 경우 | 정렬 필드 3개 이하 | 정렬 필드 많고 확장 잦을 때 |