package com.empresa.produto.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * DTO de saída — contrato da resposta HTTP.
 *
 * Inclui campos computados do domínio (temEstoque, estaDisponivel)
 * para que o cliente não precise recalcular.
 *
 * Separado do modelo de domínio — mudanças no contrato HTTP não afetam o domínio.
 */
public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        String categoria,
        boolean ativo,
        boolean temEstoque,
        boolean estaDisponivel
) {}
