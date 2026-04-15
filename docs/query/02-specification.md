# Spring Data JPA Specification

## 개요

`Specification<T>`은 Spring Data JPA가 Criteria API 위에 얹은 **경량 추상화 레이어**입니다.
조건(Predicate) 하나를 객체로 캡슐화하고, `and()` / `or()` / `not()`으로 **조합(Composite Pattern)**할 수 있어
동적 쿼리를 작은 단위로 쪼개어 관리할 수 있습니다.

---

## 핵심 인터페이스

```java
// Spring Data JPA 제공
@FunctionalInterface
public interface Specification<T> {
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);

    default Specification<T> and(Specification<T> other) { ... }
    default Specification<T> or(Specification<T> other) { ... }
    static <T> Specification<T> not(Specification<T> spec) { ... }
    static <T> Specification<T> where(Specification<T> spec) { ... }
}
```

### Repository 설정

```java
public interface IndexDataRepository
        extends JpaRepository<IndexData, Long>,
                JpaSpecificationExecutor<IndexData> {  // ← 이것만 추가
}
```

`JpaSpecificationExecutor`를 상속하면 아래 메서드를 자동으로 사용할 수 있습니다.

```java
List<T>   findAll(Specification<T> spec);
Page<T>   findAll(Specification<T> spec, Pageable pageable);
List<T>   findAll(Specification<T> spec, Sort sort);
Optional<T> findOne(Specification<T> spec);
long      count(Specification<T> spec);
```

---

## Specification 클래스 작성 패턴

### 방법 1 — static 팩토리 메서드 (권장)

```java
public class IndexDataSpec {

    public static Specification<IndexData> hasIndexInfoId(Long indexInfoId) {
        return (root, query, cb) ->
            indexInfoId == null ? null
                : cb.equal(root.get("indexInfo").get("id"), indexInfoId);
    }

    public static Specification<IndexData> baseDateGoe(LocalDate from) {
        return (root, query, cb) ->
            from == null ? null
                : cb.greaterThanOrEqualTo(root.get("baseDate"), from);
    }

    public static Specification<IndexData> baseDateLoe(LocalDate to) {
        return (root, query, cb) ->
            to == null ? null
                : cb.lessThanOrEqualTo(root.get("baseDate"), to);
    }

    public static Specification<IndexData> hasSourceType(SourceType sourceType) {
        return (root, query, cb) ->
            sourceType == null ? null
                : cb.equal(root.get("sourceType"), sourceType);
    }
}
```

> **팁**: `null`을 반환하면 Spring Data JPA가 해당 조건을 자동으로 무시합니다.
> 별도의 `if` 분기가 필요 없습니다.

---

## 동적 쿼리 조합 (서비스 레이어)

```java
@Service
@RequiredArgsConstructor
public class IndexDataService {

    private final IndexDataRepository indexDataRepository;

    public List<IndexData> search(IndexDataSearchRequest req) {
        Specification<IndexData> spec = Specification
            .where(IndexDataSpec.hasIndexInfoId(req.getIndexInfoId()))
            .and(IndexDataSpec.baseDateGoe(req.getStartDate()))
            .and(IndexDataSpec.baseDateLoe(req.getEndDate()))
            .and(IndexDataSpec.hasSourceType(req.getSourceType()));

        return indexDataRepository.findAll(spec, Sort.by("baseDate").ascending());
    }
}
```

---

## 커서 페이지네이션과 조합

`JpaSpecificationExecutor`는 `Pageable`을 지원하지만 Findex는 offset 방식 대신
**lastId 커서** 방식을 사용합니다. 커서 조건도 Specification으로 표현할 수 있습니다.

```java
public static Specification<IndexData> idLessThan(Long lastId) {
    return (root, query, cb) ->
        lastId == null ? null : cb.lessThan(root.get("id"), lastId);
}
```

```java
Specification<IndexData> spec = Specification
    .where(IndexDataSpec.hasIndexInfoId(indexInfoId))
    .and(IndexDataSpec.idLessThan(lastId));

List<IndexData> result = indexDataRepository.findAll(
    spec,
    PageRequest.of(0, pageSize + 1, Sort.by("id").descending())
).getContent();
```

---

## 중복 조인 방지 (Distinct + fetch join)

집계 쿼리와 데이터 조회 쿼리가 분리되어야 할 경우 `CriteriaQuery`의 타입을 확인해서
fetch join을 선택적으로 적용합니다.

```java
public static Specification<IndexData> withIndexInfo() {
    return (root, query, cb) -> {
        // count 쿼리에서는 fetch join 생략
        if (Long.class != query.getResultType()) {
            root.fetch("indexInfo", JoinType.LEFT);
        }
        return cb.conjunction(); // 조건 없음 (항상 true)
    };
}
```

---

## 장단점

### 장점
- 조건 단위를 **재사용 가능한 객체**로 캡슐화
- `and()` / `or()` 체이닝으로 **가독성 향상**
- Repository 메서드를 늘리지 않아도 됨
- 테스트가 용이 (Specification 단위로 단독 테스트 가능)

### 단점
- 복잡한 프로젝션(DTO 반환), 서브쿼리, 집계에는 여전히 불편
- `root.get("fieldName")` 문자열 사용 → MetaModel(`IndexData_`) 없으면 타입 안전 X
- Criteria API의 장황함을 완전히 제거하지는 못함

---

## MetaModel로 타입 안전성 강화 (선택)

`hibernate-jpamodelgen` 플러그인을 추가하면 `IndexData_.baseDate` 같은 정적 타입 경로를 사용할 수 있습니다.

```groovy
// build.gradle
dependencies {
    annotationProcessor "org.hibernate.orm:hibernate-jpamodelgen:6.x.x"
}
```

```java
// 문자열 대신 타입 안전 경로 사용
cb.greaterThanOrEqualTo(root.get(IndexData_.baseDate), from)
```

---

## 언제 사용할까?

| 상황 | 권장 여부 |
|---|---|
| 필드 몇 개로 동적 필터링 | **적합** |
| 조건 재사용, 테스트 중시 | **적합** |
| 복잡한 JOIN + DTO 프로젝션 | QueryDSL 고려 |
| 서브쿼리·윈도우 함수 | QueryDSL 또는 네이티브 쿼리 고려 |

---

## 참고

- [Spring Data JPA Docs – Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)
- Criteria API 기초 → [01-criteria-api.md](./01-criteria-api.md)
- QueryDSL 비교 → [03-querydsl.md](./03-querydsl.md)
