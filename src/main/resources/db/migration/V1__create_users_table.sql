-- V1__create_users_table.sql
CREATE TABLE users (
    id               BIGSERIAL PRIMARY KEY,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    name             VARCHAR(255) NOT NULL,
    role             VARCHAR(50)  NOT NULL DEFAULT 'USER',
    telegram_chat_id VARCHAR(100)
);

