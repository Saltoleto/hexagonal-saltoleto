-- V4__rename_and_create_saldo_limite.sql
-- Renomeia tabela produtos → produtos_financeiros e cria saldos e limites.

RENAME TABLE produtos TO produtos_financeiros;

CREATE TABLE saldos (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    valor                 DECIMAL(15, 2) NOT NULL,
    produto_financeiro_id BIGINT         NOT NULL,

    CONSTRAINT fk_saldos_produto_financeiro
        FOREIGN KEY (produto_financeiro_id)
        REFERENCES produtos_financeiros (id),

    INDEX idx_saldos_produto_financeiro_id (produto_financeiro_id),
    INDEX idx_saldos_valor                 (valor)
);

CREATE TABLE limites (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    valor                 DECIMAL(15, 2) NOT NULL,
    produto_financeiro_id BIGINT         NOT NULL,

    CONSTRAINT fk_limites_produto_financeiro
        FOREIGN KEY (produto_financeiro_id)
        REFERENCES produtos_financeiros (id),

    INDEX idx_limites_produto_financeiro_id (produto_financeiro_id),
    INDEX idx_limites_valor                 (valor)
);
