package com.empresa.produto.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record ProdutoFinanceiroResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        String categoria,
        boolean ativo,
        boolean temEstoque,
        boolean estaDisponivel,
        SaldoResponse saldo,
        LimiteResponse limite
) {
    public record SaldoResponse(Long id, BigDecimal valor) {}
    public record LimiteResponse(Long id, BigDecimal valor) {}
}
