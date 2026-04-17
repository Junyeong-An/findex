package com.sprint.mission.findex.domain.autosyncconfig.entity;

import com.sprint.mission.findex.domain.indexinfo.entity.IndexInfo;
import com.sprint.mission.findex.global.common.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "auto_sync_config")
public class AutoSyncConfig extends BaseUpdatableEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false, unique = true)
  private IndexInfo indexInfo;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = false;

  public AutoSyncConfig(IndexInfo indexInfo) {
    this.indexInfo = indexInfo;
    this.enabled = false;
  }

  public void updateEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
