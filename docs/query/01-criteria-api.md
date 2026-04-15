# JPA Criteria API

## 개요

Criteria API는 JPA 2.0부터 제공되는 **타입 세이프(type-safe) 동적 쿼리 빌더**입니다.
JPQL을 문자열 대신 Java 객체 모델로 표현하므로, 컴파일 타임에 문법 오류를 잡을 수 있습니다.

---

## 핵심 구성 요소

| 클래스 / 인터페이스 | 역할 |
|---|---|
| `CriteriaBuilder` | 쿼리·조건식·집계함수 등 모든 표현식 생성 |
| `CriteriaQuery<T>` | SELECT 쿼리 정의 (반환 타입 T) |
| `Root<T>` | FROM 절에 해당하는 엔티티 루트 |
| `Predicate` | WHERE 조건 하나의 단위 |
| `Path<X>` | 필드 경로 (`root.get("fieldName")`) |

---

## 기본 사용법

### 1. EntityManager 주입

```java
@Repository
@RequiredArgsConstructor
public class IndexDataCustomRepositoryImpl implements IndexDataCustomRepository {

    private final EntityManager em;
}
```

### 2. 단순 조회 예시

```java
public List<IndexData> findByIndexInfoId(Long indexInfoId) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<IndexData> query = cb.createQuery(IndexData.class);
    Root<IndexData> root = query.from(IndexData.class);

    query.select(root)
         .where(cb.equal(root.get("indexInfo").get("id"), indexInfoId));

    return em.createQuery(query).getResultList();
}
```

### 3. 동적 조건 조합 (Findex 활용 예시)

```java
public List<IndexData> search(Long indexInfoId, LocalDate startDate, LocalDate endDate) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<IndexData> query = cb.createQuery(IndexData.class);
    Root<IndexData> root = query.from(IndexData.class);

    List<Predicate> predicates = new ArrayList<>();

    if (indexInfoId != null) {
        predicates.add(cb.equal(root.get("indexInfo").get("id"), indexInfoId));
    }
    if (startDate != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("baseDate"), startDate));
    }
    if (endDate != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("baseDate"), endDate));
    }

    query.select(root)
         .where(cb.and(predicates.toArray(new Predicate[0])))
         .orderBy(cb.asc(root.get("baseDate")));

    return em.createQuery(query).getResultList();
}
```

---

## 커서 페이지네이션 적용 예시

Findex는 offset 대신 `lastId` 커서 방식을 사용합니다.

```java
public List<IndexData> findWithCursor(IndexDataSearchCondition cond, Long lastId, int pageSize) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<IndexData> query = cb.createQuery(IndexData.class);
    Root<IndexData> root = query.from(IndexData.class);

    List<Predicate> predicates = new ArrayList<>();

    // 기본 조건
    if (cond.getIndexInfoId() != null) {
        predicates.add(cb.equal(root.get("indexInfo").get("id"), cond.getIndexInfoId()));
    }

    // 커서 조건
    if (lastId != null) {
        predicates.add(cb.lessThan(root.get("id"), lastId));
    }

    query.select(root)
         .where(cb.and(predicates.toArray(new Predicate[0])))
         .orderBy(cb.desc(root.get("id")));

    return em.createQuery(query)
             .setMaxResults(pageSize)
             .getResultList();
}
```

---

## 장단점

### 장점
- **컴파일 타임 검증**: 오타로 인한 런타임 오류 방지
- **동적 쿼리** 조합이 명시적 (if문으로 조건 추가)
- Spring Data JPA의 `JpaSpecificationExecutor`와 통합 용이

### 단점
- 코드가 **장황(verbose)**하여 가독성이 낮음
- 단순 쿼리에도 `CriteriaBuilder`, `Root`, `Predicate` 등 보일러플레이트 필요
- JOIN이 복잡해질수록 코드 복잡도 급증

---

## 언제 사용할까?

- `Specification` API 없이 직접 동적 쿼리를 제어해야 할 때
- 서브쿼리, 집계, 커스텀 정렬 등 복잡한 SQL을 타입 세이프하게 작성해야 할 때
- QueryDSL 의존성을 추가하기 어려운 환경

---

## 참고

- [Jakarta Persistence 3.1 Specification – Criteria API](https://jakarta.ee/specifications/persistence/)
- Spring Data JPA `JpaSpecificationExecutor` → [02-specification.md](./02-specification.md)
