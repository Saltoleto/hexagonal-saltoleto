package com.empresa.produto.domain.model;

import java.math.BigDecimal;

/**
 * Objeto de valor — limite associado a um ProdutoFinanceiro (relação 1:1).
 * Null quando não cadastrado para o produto.
 */
public record Limite(
        Long id,
        BigDecimal valor,
        Long produtoFinanceiroId
) {}
