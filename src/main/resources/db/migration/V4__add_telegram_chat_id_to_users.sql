-- V4__add_telegram_chat_id_to_users.sql
-- Garante que a coluna telegram_chat_id exista na tabela users.
-- O ALTER é seguro em ambientes onde V1 já criou a coluna: o IF NOT EXISTS evita erro.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS telegram_chat_id VARCHAR(100);

UPDATE users set telegram_chat_id = '8032680728' where telegram_chat_id IS NULL;