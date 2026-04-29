package com.empresa.produto.domain.port.in;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.model.ResultadoPaginado;

import java.math.BigDecimal;

/**
 * Port de entrada — contrato do caso de uso de listagem de produtos.
 *
 * Zero dependência de framework. Pagina e ResultadoPaginado são tipos do próprio domínio.
 */
public interface ListarProdutosUseCase {

    record Filtro(
            String nome,
            String categoria,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Boolean ativo
    ) {}

    ResultadoPaginado<Produto> executar(Filtro filtro, Pagina pagina);
}
