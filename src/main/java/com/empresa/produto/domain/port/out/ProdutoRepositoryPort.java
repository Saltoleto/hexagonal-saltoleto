package com.empresa.produto.domain.port.out;

import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase.Filtro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port de saída — contrato que o domínio exige da persistência.
 *
 * O domínio define esta interface. A infraestrutura a implementa.
 * Inversão de dependência: domínio não depende de JPA, só de sua própria abstração.
 */
public interface ProdutoRepositoryPort {
    Page<Produto> listar(Filtro filtro, Pageable pageable);
}
