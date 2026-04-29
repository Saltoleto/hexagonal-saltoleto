package com.empresa.produto.domain.port.in;

import com.empresa.produto.domain.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * Port de entrada — contrato do caso de uso de listagem de produtos.
 *
 * Define O QUÊ a aplicação faz, sem revelar COMO faz.
 * O Filtro como record interno mantém o contrato coeso e evita proliferação de classes.
 */
public interface ListarProdutosUseCase {

    record Filtro(
            String nome,
            String categoria,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Boolean ativo
    ) {}

    Page<Produto> executar(Filtro filtro, Pageable pageable);
}
