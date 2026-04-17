# QueryDSL 구현 패턴 비교

QueryDSL을 Spring Data JPA와 함께 사용하는 대표적인 3가지 패턴 비교.

---

## 패턴 1. JPAQueryFactory 직접 주입

Repository 구현체에 `JPAQueryFactory`를 직접 주입받아 사용하는 가장 단순한 방식.

```java
@Repository
@RequiredArgsConstructor
public class IndexInfoQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<IndexInfo> search(IndexInfoSearchCondition cond) {
        return queryFactory
            .selectFrom(indexInfo)
            .where(
                likeIndexClassification(cond.indexClassification()),
                likeIndexName(cond.indexName()),
                eqFavorite(cond.favorite())
            )
            .fetch();
    }

    private BooleanExpression likeIndexClassification(String value) {
        return value != null ? indexInfo.indexClassification.contains(value) : null;
    }
}
```

- 장점: 구조 단순, 별도 인터페이스 불필요
- 단점: Spring Data JPA Repository와 별개의 Bean으로 존재 → 호출부에서 두 Repository를 따로 주입해야 함

---

## 패턴 2. Custom Repository 인터페이스 분리 (Spring Data 권장)

커스텀 인터페이스를 정의하고 구현체를 작성한 뒤, 기본 `JpaRepository`에 합성하는 방식.
Spring Data JPA가 `Impl` 접미사를 자동으로 인식해 구현체를 연결함.

```java
// 1. 커스텀 인터페이스
public interface IndexInfoCustomRepository {
    List<IndexInfo> search(IndexInfoSearchCondition cond);
}

// 2. 구현체 (이름 규칙: 기본 Repository 이름 + Impl)
@RequiredArgsConstructor
public class IndexInfoRepositoryImpl implements IndexInfoCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<IndexInfo> search(IndexInfoSearchCondition cond) {
        return queryFactory
            .selectFrom(indexInfo)
            .where(
                likeIndexClassification(cond.indexClassification()),
                likeIndexName(cond.indexName()),
                eqFavorite(cond.favorite())
            )
            .fetch();
    }
}

// 3. 기본 Repository에 합성
public interface IndexInfoRepository
        extends JpaRepository<IndexInfo, UUID>, IndexInfoCustomRepository {
}
```

- 장점: `IndexInfoRepository` 하나로 기본 CRUD + 동적 쿼리 모두 사용 가능, Spring Data 공식 권장
- 단점: 인터페이스 + 구현체 파일이 늘어남

---

## 패턴 3. QuerydslRepositorySupport 상속

Spring Data가 제공하는 `QuerydslRepositorySupport`를 상속받아 `EntityManager`를 자동으로 주입받는 방식.

```java
public class IndexInfoRepositoryImpl
        extends QuerydslRepositorySupport
        implements IndexInfoCustomRepository {

    public IndexInfoRepositoryImpl() {
        super(IndexInfo.class);
    }

    @Override
    public List<IndexInfo> search(IndexInfoSearchCondition cond) {
        return from(indexInfo)
            .where(
                likeIndexClassification(cond.indexClassification()),
                likeIndexName(cond.indexName()),
                eqFavorite(cond.favorite())
            )
            .fetch();
    }
}
```

- 장점: `EntityManager` 직접 관리 불필요, `from()` 등 편의 메서드 제공
- 단점: `JPAQueryFactory`를 직접 쓰지 않아 최신 QueryDSL API 일부 사용 불편, `@SpringBootTest` 없이 단위 테스트 어려움

---

## 비교 요약

| | 패턴 1 | 패턴 2 | 패턴 3 |
|---|---|---|---|
| 구조 복잡도 | 낮음 | 중간 | 중간 |
| Spring Data 통합 | X (별도 Bean) | O (합성) | O (합성) |
| JPAQueryFactory 사용 | O | O | 제한적 |
| 테스트 용이성 | 높음 | 높음 | 낮음 |
| Spring 공식 권장 | X | **O** | X |

---

## 권장

**패턴 2** 채택.
`JpaRepository`와 커스텀 쿼리를 하나의 인터페이스로 통합할 수 있어 호출부가 단순하고, Spring Data JPA 공식 권장 방식으로 유지보수에 유리함.