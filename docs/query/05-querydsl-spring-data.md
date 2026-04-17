# QueryDSL + Spring Data JPA 공식 예제

Spring Data JPA 공식 문서 기준 Custom Repository 패턴(안 B) 구현 예시.

> 참고: https://docs.spring.io/spring-data/jpa/reference/repositories/custom-implementations.html

---

## 패키지 구조

```
domain/
  indexinfo/
    repository/
      IndexInfoRepository.java
      querydsl/
        IndexInfoCustomRepository.java
        impl/
          IndexInfoCustomRepositoryImpl.java
```

---

## 기본 구조

### 커스텀 인터페이스

```java
public interface IndexInfoCustomRepository {
    List<IndexInfo> query(IndexInfoSearchCondition cond);
}
```

### 구현체

```java
import static com.sprint.mission.findex.domain.indexinfo.entity.QIndexInfo.indexInfo;

@RequiredArgsConstructor
public class IndexInfoCustomRepositoryImpl implements IndexInfoCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<IndexInfo> query(IndexInfoSearchCondition cond) {
        return queryFactory
            .selectFrom(indexInfo)
            .where(
                likeIndexClassification(cond.indexClassification()),
                likeIndexName(cond.indexName()),
                eqFavorite(cond.favorite())
            )
            .orderBy(orderSpecifier(cond.sortField(), cond.sortDirection()))
            .limit(cond.size() + 1)   // hasNext 판별용 +1
            .fetch();
    }
}
```

### 기본 Repository에 합성

구현체 이름 규칙: **인터페이스명 + `Impl`**

```java
public interface IndexInfoRepository
        extends JpaRepository<IndexInfo, UUID>, IndexInfoCustomRepository {
}
```

---

## BooleanExpression — 동적 조건

`null` 반환 시 QueryDSL이 해당 조건을 자동으로 무시함. `where()`에 나열하면 AND 처리.

### 완전 일치

```java
// Boolean
private BooleanExpression eqFavorite(Boolean value) {
    return value != null ? indexInfo.favorite.eq(value) : null;
}

// Enum
private BooleanExpression eqSourceType(SourceType value) {
    return value != null ? indexData.sourceType.eq(value) : null;
}

// UUID (FK)
private BooleanExpression eqIndexInfoId(UUID id) {
    return id != null ? indexData.indexInfo.id.eq(id) : null;
}
```

### 부분 일치

```java
// LIKE %value% (대소문자 구분)
private BooleanExpression likeIndexName(String value) {
    return value != null ? indexInfo.indexName.contains(value) : null;
}

// LIKE %value% (대소문자 무시)
private BooleanExpression likeIndexNameIgnoreCase(String value) {
    return value != null ? indexInfo.indexName.containsIgnoreCase(value) : null;
}
```

### 범위 조건

```java
// 이상 (>=)
private BooleanExpression goeBaseDate(LocalDate from) {
    return from != null ? indexData.baseDate.goe(from) : null;
}

// 이하 (<=)
private BooleanExpression loeBaseDate(LocalDate to) {
    return to != null ? indexData.baseDate.loe(to) : null;
}

// between (양쪽 null 개별 처리)
private BooleanExpression betweenBaseDate(LocalDate from, LocalDate to) {
    if (from != null && to != null) return indexData.baseDate.between(from, to);
    if (from != null) return indexData.baseDate.goe(from);
    if (to != null) return indexData.baseDate.loe(to);
    return null;
}
```

### 커서 조건 (페이지네이션)

```java
// 단순 id 커서
private BooleanExpression ltId(UUID lastId) {
    return lastId != null ? indexInfo.id.lt(lastId) : null;
}

// 복합 커서 (정렬 필드 + id 보조 정렬)
// "lastBaseDate보다 이전이거나, 같은 날짜면 lastId보다 작은 것"
private BooleanExpression cursorCondition(LocalDate lastBaseDate, UUID lastId) {
    if (lastBaseDate == null || lastId == null) return null;
    return indexData.baseDate.lt(lastBaseDate)
        .or(indexData.baseDate.eq(lastBaseDate)
            .and(indexData.id.lt(lastId)));
}
```

---

## OrderSpecifier — 동적 정렬

`sortField`와 `sortDirection`을 받아 런타임에 정렬 조건을 생성함.

```java
private OrderSpecifier<?> orderSpecifier(String sortField, String sortDirection) {
    Order direction = "desc".equalsIgnoreCase(sortDirection) ? Order.DESC : Order.ASC;

    return switch (sortField) {
        case "indexName" -> new OrderSpecifier<>(direction, indexInfo.indexName);
        case "employedItemsCount" -> new OrderSpecifier<>(direction, indexInfo.employedItemsCount);
        default -> new OrderSpecifier<>(direction, indexInfo.indexClassification);
    };
}
```

보조 정렬(tiebreaker) 추가 시:

```java
.orderBy(orderSpecifier(cond.sortField(), cond.sortDirection()), indexInfo.id.asc())
```

---

## 페이지네이션

### Slice 기반 (hasNext만 필요할 때)

```java
List<IndexInfo> result = queryFactory
    .selectFrom(indexInfo)
    .where(...)
    .orderBy(...)
    .limit(size + 1)    // 한 개 더 가져와서 hasNext 판별
    .fetch();

boolean hasNext = result.size() > size;
List<IndexInfo> content = hasNext ? result.subList(0, size) : result;
```

### Page 기반 (totalElements 필요할 때)

```java
List<IndexInfo> content = queryFactory
    .selectFrom(indexInfo)
    .where(...)
    .orderBy(...)
    .offset(pageable.getOffset())
    .limit(pageable.getPageSize())
    .fetch();

Long total = queryFactory
    .select(indexInfo.count())
    .from(indexInfo)
    .where(...)
    .fetchOne();

return new PageImpl<>(content, pageable, total);
```

---

## JOIN

```java
// fetchJoin: 연관 엔티티를 한 번의 쿼리로 함께 조회 (N+1 방지)
queryFactory
    .selectFrom(indexData)
    .join(indexData.indexInfo, indexInfo).fetchJoin()
    .where(...)
    .fetch();

// 일반 join (조건 필터링 용도, 조회는 안 함)
queryFactory
    .selectFrom(indexData)
    .join(indexData.indexInfo, indexInfo)
    .where(indexInfo.favorite.isTrue())
    .fetch();
```

---

## 검색 조건 DTO

```java
public record IndexInfoSearchCondition(
    String indexClassification,
    String indexName,
    Boolean favorite,
    String sortField,
    String sortDirection,
    int size,
    UUID lastId
) {}
```

---

## 주의 사항

- Q클래스는 `./gradlew compileJava` 로 생성, 엔티티 변경 시 재생성 필요
- `where()` 에 나열된 조건은 AND 처리, OR는 `.or()` 또는 `BooleanBuilder` 사용
- `fetchJoin()`은 `distinct()`와 함께 쓰지 않으면 컬렉션 조인 시 중복 발생 가능
- `totalElements` 조회 쿼리는 별도로 실행됨 (count 쿼리 분리)