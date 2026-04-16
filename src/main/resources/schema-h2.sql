CREATE TABLE IF NOT EXISTS index_info (
    id                   UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    index_classification VARCHAR(240) NOT NULL,
    index_name           VARCHAR(240) NOT NULL,
    employed_items_count INTEGER      NOT NULL,
    base_point_in_time   DATE         NOT NULL,
    base_index           NUMERIC(20, 4) NOT NULL,
    source_type          VARCHAR(10)  NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
    favorite             BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (index_classification, index_name)
);

CREATE TABLE IF NOT EXISTS index_data (
    id                  UUID        DEFAULT RANDOM_UUID() PRIMARY KEY,
    created_at          TIMESTAMP   NOT NULL,
    updated_at          TIMESTAMP   NOT NULL,
    index_info_id       UUID        NOT NULL,
    base_date           DATE        NOT NULL,
    source_type         VARCHAR(10) NOT NULL CHECK (source_type IN ('USER', 'OPEN_API')),
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

CREATE TABLE IF NOT EXISTS sync_job (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    created_at    TIMESTAMP    NOT NULL,
    index_info_id UUID         NOT NULL,
    job_type      VARCHAR(20)  NOT NULL CHECK (job_type IN ('INDEX_INFO', 'INDEX_DATA')),
    target_date   DATE,
    worker        VARCHAR(100) NOT NULL,
    job_time      TIMESTAMP    NOT NULL,
    result        VARCHAR(10)  NOT NULL CHECK (result IN ('SUCCESS', 'FAILED')),
    error_message TEXT,
    FOREIGN KEY (index_info_id) REFERENCES index_info (id)
);

CREATE TABLE IF NOT EXISTS auto_sync_config (
    id            UUID      DEFAULT RANDOM_UUID() PRIMARY KEY,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL,
    index_info_id UUID      NOT NULL UNIQUE,
    enabled       BOOLEAN   NOT NULL DEFAULT FALSE,
    FOREIGN KEY (index_info_id) REFERENCES index_info (id) ON DELETE CASCADE
);