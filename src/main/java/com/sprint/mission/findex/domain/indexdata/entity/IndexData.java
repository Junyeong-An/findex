package com.sprint.mission.findex.domain.indexdata.entity;

import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.domain.indexinfo.entity.SourceType;
import com.sprint.mission.findex.global.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "index_data",
    uniqueConstraints = @UniqueConstraint(columnNames = {"index_info_id", "base_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexData extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 10, columnDefinition = "VARCHAR(10)")
  private SourceType sourceType;

  @Column(name = "market_price", nullable = false, precision = 20, scale = 4)
  private BigDecimal marketPrice;

  @Column(name = "closing_price", nullable = false, precision = 20, scale = 4)
  private BigDecimal closingPrice;

  @Column(name = "high_price", nullable = false, precision = 20, scale = 4)
  private BigDecimal highPrice;

  @Column(name = "low_price", nullable = false, precision = 20, scale = 4)
  private BigDecimal lowPrice;

  @Column(name = "versus", nullable = false, precision = 20, scale = 4)
  private BigDecimal versus;

  @Column(name = "fluctuation_rate", nullable = false, precision = 10, scale = 4)
  private BigDecimal fluctuationRate;

  @Column(name = "trading_quantity", nullable = false)
  private Long tradingQuantity;

  @Column(name = "trading_price", nullable = false, precision = 30, scale = 4)
  private BigDecimal tradingPrice;

  @Column(name = "market_total_amount", nullable = false, precision = 30, scale = 4)
  private BigDecimal marketTotalAmount;

  @Builder
  public IndexData(IndexInfo indexInfo, LocalDate baseDate, SourceType sourceType,
      BigDecimal marketPrice, BigDecimal closingPrice,
      BigDecimal highPrice, BigDecimal lowPrice,
      BigDecimal versus, BigDecimal fluctuationRate,
      Long tradingQuantity, BigDecimal tradingPrice,
      BigDecimal marketTotalAmount) {
    this.indexInfo = indexInfo;
    this.baseDate = baseDate;
    this.sourceType = sourceType;
    this.marketPrice = marketPrice;
    this.closingPrice = closingPrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.versus = versus;
    this.fluctuationRate = fluctuationRate;
    this.tradingQuantity = tradingQuantity;
    this.tradingPrice = tradingPrice;
    this.marketTotalAmount = marketTotalAmount;
  }

  public void update(BigDecimal marketPrice, BigDecimal closingPrice,
      BigDecimal highPrice, BigDecimal lowPrice,
      BigDecimal versus, BigDecimal fluctuationRate,
      Long tradingQuantity, BigDecimal tradingPrice,
      BigDecimal marketTotalAmount) {
    if (marketPrice != null) this.marketPrice = marketPrice;
    if (closingPrice != null) this.closingPrice = closingPrice;
    if (highPrice != null) this.highPrice = highPrice;
    if (lowPrice != null) this.lowPrice = lowPrice;
    if (versus != null) this.versus = versus;
    if (fluctuationRate != null) this.fluctuationRate = fluctuationRate;
    if (tradingQuantity != null) this.tradingQuantity = tradingQuantity;
    if (tradingPrice != null) this.tradingPrice = tradingPrice;
    if (marketTotalAmount != null) this.marketTotalAmount = marketTotalAmount;
  }
}