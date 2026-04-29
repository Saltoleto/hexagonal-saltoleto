-- V1__create_produtos.sql
-- Criação da tabela de produtos com índices para os filtros mais comuns.

CREATE TABLE produtos (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(150)   NOT NULL,
    descricao TEXT,
    preco     DECIMAL(10, 2) NOT NULL,
    estoque   INT            NOT NULL DEFAULT 0,
    categoria VARCHAR(50)    NOT NULL,
    ativo     BOOLEAN        NOT NULL DEFAULT TRUE,

    INDEX idx_produtos_nome      (nome),
    INDEX idx_produtos_categoria (categoria),
    INDEX idx_produtos_ativo     (ativo),
    INDEX idx_produtos_preco     (preco)
);
