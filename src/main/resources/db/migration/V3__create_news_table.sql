-- V3__create_news_table.sql
CREATE TABLE news (
    id          BIGSERIAL PRIMARY KEY,
    stock_id    BIGINT        NOT NULL REFERENCES stocks(id) ON DELETE CASCADE,
    title       VARCHAR(512)  NOT NULL,
    description VARCHAR(1024),
    url         VARCHAR(1024) NOT NULL,
    source_name VARCHAR(255),
    fetched_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    notified    BOOLEAN       NOT NULL DEFAULT FALSE,
    UNIQUE (stock_id, url)
);

CREATE INDEX idx_news_stock_id  ON news(stock_id);
CREATE INDEX idx_news_notified  ON news(notified);
CREATE INDEX idx_news_fetched   ON news(fetched_at DESC);

