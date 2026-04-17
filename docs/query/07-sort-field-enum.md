# 정렬 필드 Enum 패턴

동적 정렬과 커서 추출 로직을 enum에 캡슐화하는 방식.
switch 분기가 여러 곳에 흩어지지 않고 enum 하나에 집중됨.

---

## 패키지 위치

```
domain/
  indexinfo/
    repository/
      querydsl/
        IndexInfoSortField.java    # 정렬 필드 enum
        IndexInfoCustomRepository.java
        impl/
          IndexInfoCustomRepositoryImpl.java
```

도메인별로 각자 `repository/querydsl/` 하위에 위치.

---

## 구현

```java
public enum IndexInfoSortField {

    INDEX_CLASSIFICATION("indexClassification") {
        @Override
        public ComparableExpressionBase<?> getSortField(QIndexInfo q) {
            return q.indexClassification;
        }
        @Override
        public String getCursor(IndexInfoResponse dto) {
            return dto.indexClassification();
        }
        @Override
        public BooleanExpression buildCursorCondition(
                QIndexInfo q, String cursor, UUID idAfter, boolean asc) {
            return buildCursorCondition(
                asc ? q.indexClassification.gt(cursor) : q.indexClassification.lt(cursor),
                q.indexClassification.eq(cursor),
                q.id, idAfter, asc
            );
        }
    },

    INDEX_NAME("indexName") {
        @Override
        public ComparableExpressionBase<?> getSortField(QIndexInfo q) {
            return q.indexName;
        }
        @Override
        public String getCursor(IndexInfoResponse dto) {
            return dto.indexName();
        }
        @Override
        public BooleanExpression buildCursorCondition(
                QIndexInfo q, String cursor, UUID idAfter, boolean asc) {
            return buildCursorCondition(
                asc ? q.indexName.gt(cursor) : q.indexName.lt(cursor),
                q.indexName.eq(cursor),
                q.id, idAfter, asc
            );
        }
    },

    EMPLOYED_ITEMS_COUNT("employedItemsCount") {
        @Override
        public ComparableExpressionBase<?> getSortField(QIndexInfo q) {
            return q.employedItemsCount;
        }
        @Override
        public String getCursor(IndexInfoResponse dto) {
            return String.valueOf(dto.employedItemsCount());
        }
        @Override
        public BooleanExpression buildCursorCondition(
                QIndexInfo q, String cursor, UUID idAfter, boolean asc) {
            Integer cursorValue = Integer.parseInt(cursor);
            return buildCursorCondition(
                asc ? q.employedItemsCount.gt(cursorValue) : q.employedItemsCount.lt(cursorValue),
                q.employedItemsCount.eq(cursorValue),
                q.id, idAfter, asc
            );
        }
    };

    private final String value;

    IndexInfoSortField(String value) { this.value = value; }

    public abstract ComparableExpressionBase<?> getSortField(QIndexInfo q);
    public abstract String getCursor(IndexInfoResponse dto);
    public abstract BooleanExpression buildCursorCondition(
            QIndexInfo q, String cursor, UUID idAfter, boolean asc);

    // 공통 커서 조건 빌더
    // "정렬값이 커서보다 크거나(asc), 같으면 id 기준으로 다음 위치"
    protected BooleanExpression buildCursorCondition(
            BooleanExpression gtOrLt,
            BooleanExpression eq,
            ComparableExpression<UUID> idField,
            UUID idAfter,
            boolean asc) {
        if (idAfter == null) return null;
        BooleanExpression idCondition = asc ? idField.gt(idAfter) : idField.lt(idAfter);
        return gtOrLt.or(eq.and(idCondition));
    }

    public OrderSpecifier<?> toOrderSpecifier(QIndexInfo q, boolean asc) {
        return asc
            ? getSortField(q).asc()
            : getSortField(q).desc();
    }

    public static IndexInfoSortField from(String value) {
        return Arrays.stream(values())
            .filter(f -> f.value.equals(value))
            .findFirst()
            .orElse(INDEX_CLASSIFICATION);
    }
}
```

---

## 사용

```java
// Repository 구현체
import static com.sprint.mission.findex.domain.indexinfo.entity.QIndexInfo.indexInfo;

IndexInfoSortField sortField = IndexInfoSortField.from(cond.sortField());
boolean asc = "asc".equalsIgnoreCase(cond.sortDirection());

List<IndexInfoResponse> content = queryFactory
    .select(/* Projections */)
    .from(indexInfo)
    .where(
        likeIndexClassification(cond.indexClassification()),
        likeIndexName(cond.indexName()),
        eqFavorite(cond.favorite()),
        sortField.buildCursorCondition(indexInfo, cond.cursor(), cond.idAfter(), asc)
    )
    .orderBy(sortField.toOrderSpecifier(indexInfo, asc), indexInfo.id.asc())
    .limit(cond.size() + 1)
    .fetch();

boolean hasNext = content.size() > cond.size();
List<IndexInfoResponse> result = hasNext ? content.subList(0, cond.size()) : content;

String nextCursor = hasNext ? sortField.getCursor(result.get(result.size() - 1)) : null;
UUID nextIdAfter = hasNext ? result.get(result.size() - 1).id() : null;
```

---

## 기존 패턴과 비교

| | 기존 (switch 분기) | Enum 패턴 |
|---|---|---|
| 정렬 필드 추가 | switch 여러 곳 수정 | enum에만 추가 |
| Q클래스 의존 | 구현체에 집중 | 파라미터로 주입 |
| 커서 추출 대상 | 엔티티 | **DTO** (엔티티 노출 없음) |
| 커서 조건 중복 | 각 case마다 반복 | 공통 helper로 제거 |
| 컴파일 타임 검증 | X | abstract 메서드로 보장 |