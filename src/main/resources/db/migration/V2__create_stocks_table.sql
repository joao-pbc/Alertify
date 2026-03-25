-- V2__create_stocks_table.sql
CREATE TABLE stocks (
    id         BIGSERIAL PRIMARY KEY,
    ticker     VARCHAR(20)  NOT NULL,
    name       VARCHAR(255),
    exchange   VARCHAR(100),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (ticker, user_id)
);

CREATE INDEX idx_stocks_user_id ON stocks(user_id);
CREATE INDEX idx_stocks_active   ON stocks(active);

