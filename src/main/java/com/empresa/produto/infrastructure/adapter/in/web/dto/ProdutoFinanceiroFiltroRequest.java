package com.empresa.produto.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProdutoFinanceiroFiltroRequest(

        @Size(max = 150, message = "Nome não pode exceder 150 caracteres")
        String nome,

        String categoria,

        @DecimalMin(value = "0.01", message = "Preço mínimo deve ser maior que zero")
        BigDecimal precoMin,

        @DecimalMin(value = "0.01", message = "Preço máximo deve ser maior que zero")
        BigDecimal precoMax,

        Boolean ativo
) {}
