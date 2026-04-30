-- V5__seed_saldo_limite.sql
-- Saldo e limite para os produtos do seed (V2). Apenas para alguns — os demais ficam null.

INSERT INTO saldos (valor, produto_financeiro_id) VALUES
(12500.00, 1),
(3200.50,  2),
(890.00,   3),
(45000.00, 5),
(1750.25,  7);

INSERT INTO limites (valor, produto_financeiro_id) VALUES
(20000.00, 1),
(5000.00,  2),
(1500.00,  3),
(60000.00, 5),
(3000.00,  7);
