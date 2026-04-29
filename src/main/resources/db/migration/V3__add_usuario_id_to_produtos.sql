-- V3__add_usuario_id_to_produtos.sql
-- Adiciona coluna usuario_id para filtro por usuário.

ALTER TABLE produtos
    ADD COLUMN usuario_id BIGINT NULL,
    ADD INDEX idx_produtos_usuario_id (usuario_id);
