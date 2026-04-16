package com.sprint.mission.findex.domain.indexinfo.entity;

import com.sprint.mission.findex.global.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "index_info", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"index_classification", "index_name"})
})
public class IndexInfo extends BaseUpdatableEntity {

  @Column(name = "index_classification", nullable = false, length = 240)
  private String indexClassification;

  @Column(name = "index_name", nullable = false, length = 240)
  private String indexName;

  @Column(name = "employed_items_count", nullable = false)
  private Integer employedItemsCount;

  @Column(name = "base_point_in_time", nullable = false, length = 50)
  private LocalDate basePointInTime;

  @Column(name = "base_index", nullable = false, precision = 20, scale = 4)
  private BigDecimal baseIndex;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 10)
  private SourceType sourceType;

  @Column(name = "favorite", nullable = false)
  private Boolean favorite = false;

  public IndexInfo(String indexClassification, String indexName, Integer employedItemsCount,
      LocalDate basePointInTime, BigDecimal baseIndex, SourceType sourceType, Boolean favorite) {
    this.indexClassification = indexClassification;
    this.indexName = indexName;
    this.employedItemsCount = employedItemsCount;
    this.basePointInTime = basePointInTime;
    this.baseIndex = baseIndex;
    this.sourceType = sourceType;
    this.favorite = favorite;
  }

  public void update(Integer employedItemsCount, LocalDate basePointInTime, BigDecimal baseIndex,
      Boolean favorite) {
    if (employedItemsCount != null) this.employedItemsCount = employedItemsCount;
    if (basePointInTime != null) this.basePointInTime = basePointInTime;
    if (baseIndex != null) this.baseIndex = baseIndex;
    if (favorite != null) this.favorite = favorite;
  }
}
