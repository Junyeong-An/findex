# 커서 기반 페이지네이션

---

## cursor vs idAfter

| | cursor | idAfter |
|---|---|---|
| 역할 | 정렬 필드의 마지막 값 | 마지막 요소 UUID |
| 용도 | 다음 페이지 시작 위치 | 동일 정렬값 내 순서 보장 (tiebreaker) |
| 타입 | String (쿼리 파라미터) | UUID |

정렬 필드값이 중복될 수 있으므로 `cursor`만으로는 정합성 보장이 안 됨.
`idAfter`를 tiebreaker로 함께 사용해야 누락/중복 없이 정확한 페이지네이션이 가능함.

---

## 복합 커서 조건 (cursorCondition)

정렬 방향에 따라 `lt` / `gt` 를 바꿔야 함.

```java
private BooleanExpression cursorCondition(String cursor, UUID idAfter,
    String sortField, String sortDirection) {
    if (cursor == null || idAfter == null) return null;

    boolean isDesc = "desc".equalsIgnoreCase(sortDirection);

    return switch (sortField) {
        case "baseDate" -> {
            LocalDate cursorDate = LocalDate.parse(cursor);
            yield isDesc
                ? indexData.baseDate.lt(cursorDate)
                    .or(indexData.baseDate.eq(cursorDate).and(indexData.id.lt(idAfter)))
                : indexData.baseDate.gt(cursorDate)
                    .or(indexData.baseDate.eq(cursorDate).and(indexData.id.gt(idAfter)));
        }
        case "closingPrice" -> {
            BigDecimal cursorPrice = new BigDecimal(cursor);
            yield isDesc
                ? indexData.closingPrice.lt(cursorPrice)
                    .or(indexData.closingPrice.eq(cursorPrice).and(indexData.id.lt(idAfter)))
                : indexData.closingPrice.gt(cursorPrice)
                    .or(indexData.closingPrice.eq(cursorPrice).and(indexData.id.gt(idAfter)));
        }
        default -> isDesc ? indexData.id.lt(idAfter) : indexData.id.gt(idAfter);
    };
}
```

---

## Slice 기반 hasNext 처리

```java
List<T> result = queryFactory
    .selectFrom(...)
    .where(...)
    .orderBy(...)
    .limit(size + 1)    // 한 개 더 가져와서 hasNext 판별
    .fetch();

boolean hasNext = result.size() > size;
List<T> content = hasNext ? result.subList(0, size) : result;
```

---

## 다음 cursor / idAfter 계산

```java
// 마지막 요소 기준으로 다음 커서 생성
T last = content.get(content.size() - 1);

UUID nextIdAfter = hasNext ? last.getId() : null;
String nextCursor = hasNext ? last.getBaseDate().toString() : null;
```

`CursorPageResponse` 직접 생성:

```java
return new CursorPageResponse<>(content, nextCursor, nextIdAfter, size, null, hasNext);
```

---

## 주의 사항

- `cursor`와 `idAfter`를 분리해서 받아야 복합 커서 조건 적용 가능
- 정렬 방향(`sortDirection`)에 따라 `lt` / `gt` 방향이 바뀜
- `cursor` 타입이 String이므로 정렬 필드 타입에 맞게 변환 필요 (`LocalDate.parse`, `new BigDecimal` 등)
- `yield`는 Java 14+ switch 표현식에서 블록 내 값 반환 시 사용