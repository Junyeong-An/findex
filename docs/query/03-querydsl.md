# QueryDSL

## 개요

QueryDSL은 **Java 코드로 SQL/JPQL을 작성**할 수 있게 해주는 오픈소스 라이브러리입니다.
APT(Annotation Processing Tool)로 엔티티마다 `Q클래스`(`QIndexData`, `QIndexInfo` 등)를 자동 생성하며,
이를 통해 **완전한 타입 안전성**과 **IDE 자동완성**을 제공합니다.

---

## 의존성 설정 (Spring Boot 3.x + Gradle)

```groovy
// build.gradle
dependencies {
    implementation "com.querydsl:querydsl-jpa:5.1.0:jakarta"
    annotationProcessor "com.querydsl:querydsl-apt:5.1.0:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

// Q클래스 생성 경로
def querydslDir = "$buildDir/generated/querydsl"

sourceSets {
    main.java.srcDirs += [querydslDir]
}

tasks.withType(JavaCompile) {
    options.getGeneratedSourceOutputDirectory().set(file(querydslDir))
}

clean.doLast {
    file(querydslDir).deleteDir()
}
```

> Q클래스 생성: `./gradlew compileJava`

---

## 핵심 구성 요소

| 클래스 | 역할 |
|---|---|
| `JPAQueryFactory` | 쿼리 진입점, `EntityManager`로 생성 |
| `QEntity` | APT가 생성한 타입 안전 메타 클래스 |
| `BooleanExpression` | `Predicate`에 해당하는 조건 표현식 |
| `BooleanBuilder` | 여러 조건을 동적으로 조합하는 빌더 |
| `Projections` | DTO로 직접 매핑 (`bean`, `constructor`, `fields`) |

---

## 기본 설정

### JPAQueryFactory Bean 등록

```java
@Configuration
public class QuerydslConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }
}
```

### Custom Repository 구조

```java
// 인터페이스
public interface IndexDataCustomRepository {
    List<IndexData> search(IndexDataSearchCondition cond, Long lastId, int pageSize);
}

// 구현체
@Repository
@RequiredArgsConstructor
public class IndexDataCustomRepositoryImpl implements IndexDataCustomRepository {

    private final JPAQueryFactory queryFactory;
}

// 기본 Repository에 합성
public interface IndexDataRepository
        extends JpaRepository<IndexData, Long>,
                IndexDataCustomRepository {
}
```

---

## 동적 쿼리 작성

### 1. BooleanExpression 조건 메서드 분리 (권장)

```java
@Repository
@RequiredArgsConstructor
public class IndexDataCustomRepositoryImpl implements IndexDataCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<IndexData> search(IndexDataSearchCondition cond, Long lastId, int pageSize) {
        return queryFactory
            .selectFrom(indexData)
            .join(indexData.indexInfo, indexInfo).fetchJoin()
            .where(
                indexInfoIdEq(cond.getIndexInfoId()),
                baseDateGoe(cond.getStartDate()),
                baseDateLoe(cond.getEndDate()),
                sourceTypeEq(cond.getSourceType()),
                idLessThan(lastId)
            )
            .orderBy(indexData.id.desc())
            .limit(pageSize)
            .fetch();
    }

    // 조건 메서드: null 반환 시 QueryDSL이 자동으로 해당 조건 무시
    private BooleanExpression indexInfoIdEq(Long indexInfoId) {
        return indexInfoId != null ? indexData.indexInfo.id.eq(indexInfoId) : null;
    }

    private BooleanExpression baseDateGoe(LocalDate from) {
        return from != null ? indexData.baseDate.goe(from) : null;
    }

    private BooleanExpression baseDateLoe(LocalDate to) {
        return to != null ? indexData.baseDate.loe(to) : null;
    }

    private BooleanExpression sourceTypeEq(SourceType sourceType) {
        return sourceType != null ? indexData.sourceType.eq(sourceType) : null;
    }

    private BooleanExpression idLessThan(Long lastId) {
        return lastId != null ? indexData.id.lt(lastId) : null;
    }
}
```

### 2. BooleanBuilder (조건이 매우 동적일 때)

```java
BooleanBuilder builder = new BooleanBuilder();

if (cond.getIndexInfoId() != null) {
    builder.and(indexData.indexInfo.id.eq(cond.getIndexInfoId()));
}
if (cond.getStartDate() != null) {
    builder.and(indexData.baseDate.goe(cond.getStartDate()));
}

return queryFactory.selectFrom(indexData).where(builder).fetch();
```

---

## DTO 직접 조회 (Projections)

서비스 레이어에 엔티티 대신 DTO를 바로 반환하면 불필요한 필드 로딩을 줄일 수 있습니다.

```java
// DTO
@QueryProjection  // ← 생성자에 붙이면 QIndexDataResponse 클래스 자동 생성
public record IndexDataResponse(
    Long id,
    LocalDate baseDate,
    BigDecimal closingPrice,
    BigDecimal fluctuationRate
) {}
```

```java
// @QueryProjection 사용
return queryFactory
    .select(new QIndexDataResponse(
        indexData.id,
        indexData.baseDate,
        indexData.closingPrice,
        indexData.fluctuationRate
    ))
    .from(indexData)
    .where(indexInfoIdEq(indexInfoId))
    .orderBy(indexData.baseDate.asc())
    .fetch();
```

```java
// Projections.constructor 사용 (어노테이션 없이)
return queryFactory
    .select(Projections.constructor(IndexDataResponse.class,
        indexData.id,
        indexData.baseDate,
        indexData.closingPrice,
        indexData.fluctuationRate
    ))
    .from(indexData)
    .fetch();
```

---

## 이동평균 (MA5/MA20) 쿼리 예시

Findex 대시보드의 이동평균은 직전 N일 데이터가 필요합니다.
QueryDSL에서 서브쿼리로 표현할 수 있습니다.

```java
// 특정 날짜 기준 직전 20일 종가 조회
public List<BigDecimal> findRecentClosingPrices(Long indexInfoId, LocalDate baseDate, int days) {
    return queryFactory
        .select(indexData.closingPrice)
        .from(indexData)
        .where(
            indexData.indexInfo.id.eq(indexInfoId),
            indexData.baseDate.loe(baseDate)
        )
        .orderBy(indexData.baseDate.desc())
        .limit(days)
        .fetch();
}
```

---

## 랭킹 쿼리 예시 (M6 Dashboard)

```java
public List<IndexRankingResponse> findTopByFluctuationRate(int topN) {
    return queryFactory
        .select(Projections.constructor(IndexRankingResponse.class,
            indexInfo.indexName,
            indexData.fluctuationRate,
            indexData.closingPrice
        ))
        .from(indexData)
        .join(indexData.indexInfo, indexInfo)
        .where(indexData.baseDate.eq(
            JPAExpressions.select(indexData.baseDate.max())
                          .from(indexData)
        ))
        .orderBy(indexData.fluctuationRate.desc())
        .limit(topN)
        .fetch();
}
```

---

## Specification vs QueryDSL 비교

| 항목 | Specification | QueryDSL |
|---|---|---|
| 설정 복잡도 | 낮음 (의존성 추가 불필요) | 중간 (APT 설정 필요) |
| 가독성 | 중간 | **높음** |
| DTO 직접 조회 | 불편 | **편리** (Projections) |
| JOIN 복잡도 | 높음 | 낮음 |
| 서브쿼리 | 불편 | **편리** (JPAExpressions) |
| 집계·윈도우 | 불편 | 중간 (네이티브 필요시 있음) |
| IDE 지원 | 보통 | **우수** (자동완성) |
| 학습 비용 | 낮음 | 중간 |

---

## 장단점

### 장점
- SQL과 유사한 구조로 **가독성 최고**
- **완전한 타입 안전성** — 컴파일 타임에 필드명·타입 검증
- 동적 조건, JOIN, 서브쿼리, DTO 매핑까지 일관된 API
- `BooleanExpression` 메서드 분리로 조건 재사용 가능

### 단점
- APT 설정 및 Q클래스 관리 필요 (빌드 설정 복잡)
- 엔티티 변경 시 Q클래스 **재생성** 필요 (`compileJava`)
- 라이브러리 버전별 jakarta/javax 네임스페이스 차이 주의

---

## 언제 사용할까?

- 조인이 많거나 동적 조건이 복잡한 쿼리
- DTO 직접 반환으로 성능 최적화가 필요할 때
- 대시보드, 랭킹, 집계 등 비즈니스 로직이 풍부한 조회
- 팀 전체가 QueryDSL에 익숙하거나 장기 유지보수를 고려할 때

---

## 참고

- [QueryDSL 공식 문서](http://querydsl.com/static/querydsl/5.0.0/reference/html_single/)
- [Spring Data JPA + QueryDSL 통합](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#core.extensions.querydsl)
- Criteria API 기초 → [01-criteria-api.md](./01-criteria-api.md)
- Specification 비교 → [02-specification.md](./02-specification.md)