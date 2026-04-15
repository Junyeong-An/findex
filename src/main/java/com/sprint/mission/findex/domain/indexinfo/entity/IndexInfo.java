package com.sprint.mission.findex.domain.indexinfo.entity;

import com.sprint.mission.findex.global.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
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
  private String basePointInTime;

  @Column(name = "base_index", nullable = false, precision = 20, scale = 4)
  private BigDecimal baseIndex;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 10)
  private SourceType sourceType;

  public enum SourceType {
    USER,
    OPEN_API
  }

  @Column(name = "favorite", nullable = false)
  private Boolean favorite = false;

  public IndexInfo(String indexClassification, String indexName, Integer employedItemsCount,
      String basePointInTime, BigDecimal baseIndex, SourceType sourceType, Boolean favorite) {
    this.indexClassification = indexClassification;
    this.indexName = indexName;
    this.employedItemsCount = employedItemsCount;
    this.basePointInTime = basePointInTime;
    this.baseIndex = baseIndex;
    this.sourceType = sourceType;
    this.favorite = favorite;
  }
}
