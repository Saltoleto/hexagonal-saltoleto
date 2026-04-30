package com.empresa.produto.domain.model;

import java.math.BigDecimal;

/**
 * Critérios de consulta para produtos financeiros.
 *
 * Todos os campos são opcionais — null significa "sem restrição".
 */
public record ProdutoFinanceiroFiltro(
        Long produtoId,
        Long usuarioId,
        String nome,
        String categoria,
        BigDecimal precoMin,
        BigDecimal precoMax,
        Boolean ativo
) {}
