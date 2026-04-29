package com.empresa.produto.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO de entrada — parâmetros de filtro recebidos via query string.
 *
 * produtoId e usuarioId NÃO estão aqui — chegam por outros mecanismos HTTP
 * (@PathVariable e @RequestParam direto no controller) e são combinados
 * com este record no momento da construção do Filtro.
 *
 * Todos os campos são opcionais.
 */
public record ProdutoFiltroRequest(

        @Size(max = 150, message = "Nome não pode exceder 150 caracteres")
        String nome,

        String categoria,

        @DecimalMin(value = "0.01", message = "Preço mínimo deve ser maior que zero")
        BigDecimal precoMin,

        @DecimalMin(value = "0.01", message = "Preço máximo deve ser maior que zero")
        BigDecimal precoMax,

        Boolean ativo
) {}
