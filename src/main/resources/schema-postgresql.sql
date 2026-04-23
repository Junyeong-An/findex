CREATE TABLE IF NOT EXISTS index_info (
    id                   UUID PRIMARY KEY,
    created_at           TIMESTAMPTZ    NOT NULL,
    updated_at           TIMESTAMPTZ    NOT NULL,
    index_classification VARCHAR(240)   NOT NULL,
    index_name           VARCHAR(240)   NOT NULL,
    employed_items_count INTEGER        NOT NULL,
    base_point_in_time   DATE           NOT NULL,
    base_index           NUMERIC(20, 4) NOT NULL,
    source_type          VARCHAR(10)    NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
    favorite             BOOLEAN        NOT NULL DEFAULT FALSE,
    UNIQUE (index_classification, index_name)
);

-- IndexInfo: 즐겨찾기 필터링 최적화 (Partial Index)
CREATE INDEX IF NOT EXISTS idx_index_info_favorite_true
    ON index_info (favorite) WHERE (favorite = TRUE);

CREATE TABLE IF NOT EXISTS index_data (
    id                  UUID PRIMARY KEY,
    created_at          TIMESTAMPTZ    NOT NULL,
    updated_at          TIMESTAMPTZ    NOT NULL,
    index_info_id       UUID           NOT NULL,
    base_date           DATE           NOT NULL,
    source_type         VARCHAR(10)    NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
    market_price        NUMERIC(20, 4) NOT NULL,
    closing_price       NUMERIC(20, 4) NOT NULL,
    high_price          NUMERIC(20, 4) NOT NULL,
    low_price           NUMERIC(20, 4) NOT NULL,
    versus              NUMERIC(20, 4) NOT NULL,
    fluctuation_rate    NUMERIC(10, 4) NOT NULL,
    trading_quantity    BIGINT         NOT NULL,
    trading_price       NUMERIC(30, 4) NOT NULL,
    market_total_amount NUMERIC(30, 4) NOT NULL,
    UNIQUE (index_info_id, base_date),
    FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
);

-- IndexData: 시계열 내림차순 조회 및 정렬 최적화
CREATE INDEX IF NOT EXISTS idx_index_data_lookup
    ON index_data (index_info_id, base_date DESC);

CREATE TABLE IF NOT EXISTS sync_job (
    id            UUID PRIMARY KEY,
    created_at    TIMESTAMPTZ  NOT NULL,
    index_info_id UUID         NOT NULL,
    job_type      VARCHAR(20)  NOT NULL CHECK (job_type IN ('INDEX_INFO', 'INDEX_DATA')),
    target_date   DATE,
    worker        VARCHAR(100) NOT NULL,
    job_time      TIMESTAMPTZ  NOT NULL,
    result        VARCHAR(10)  NOT NULL CHECK (result IN ('SUCCESS', 'FAILED')),
    error_message TEXT,
    FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
);

-- SyncJob: 최신 작업 성공 이력 조회 최적화 (Partial Index)
CREATE INDEX IF NOT EXISTS idx_sync_job_last_success
    ON sync_job (index_info_id, job_type, target_date DESC) WHERE (result = 'SUCCESS');

CREATE TABLE IF NOT EXISTS auto_sync_config (
    id            UUID PRIMARY KEY,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    index_info_id UUID        NOT NULL UNIQUE,
    enabled       BOOLEAN     NOT NULL DEFAULT FALSE,
    FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
);