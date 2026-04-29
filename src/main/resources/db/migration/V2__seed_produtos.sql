-- V2__seed_produtos.sql
-- Dados iniciais para desenvolvimento e testes manuais.

INSERT INTO produtos (nome, descricao, preco, estoque, categoria, ativo) VALUES
('Smartphone Galaxy A54', 'Tela 6.4", 128GB, câmera tripla 50MP', 1899.90, 50, 'ELETRONICO', true),
('Notebook Dell Inspiron 15', 'Intel Core i5, 8GB RAM, SSD 256GB', 3499.00, 15, 'ELETRONICO', true),
('Smart TV 55" 4K', 'QLED, HDR10+, Wi-Fi, Bluetooth', 2799.90, 8, 'ELETRONICO', true),
('Camiseta Polo Masculina', '100% algodão, várias cores', 89.90, 200, 'VESTUARIO', true),
('Tênis Running Pro', 'Amortecimento EVA, solado antiderrapante', 349.90, 75, 'VESTUARIO', true),
('Sofá Retrátil 3 Lugares', 'Suede cinza, espuma D33', 1299.00, 5, 'MOVEL', true),
('Mesa de Escritório', 'MDF 180x75cm, estrutura metálica', 549.90, 12, 'MOVEL', true),
('Whey Protein 900g', 'Sabor chocolate, 30g de proteína por dose', 149.90, 100, 'ALIMENTO', true),
('Azeite Extra Virgem 500ml', 'Origem portuguesa, acidez 0.2%', 39.90, 300, 'ALIMENTO', true),
('Fone Bluetooth JBL', 'Driver 40mm, até 20h de bateria', 299.90, 0, 'ELETRONICO', false);
