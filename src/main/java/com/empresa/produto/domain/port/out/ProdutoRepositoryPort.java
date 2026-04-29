package com.empresa.produto.domain.port.out;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.model.ResultadoPaginado;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase.Filtro;

/**
 * Port de saída — contrato que o domínio exige da persistência.
 *
 * Zero dependência de framework. O domínio define esta interface
 * em seus próprios termos — a infraestrutura se adapta a ela.
 */
public interface ProdutoRepositoryPort {
    ResultadoPaginado<Produto> listar(Filtro filtro, Pagina pagina);
}
