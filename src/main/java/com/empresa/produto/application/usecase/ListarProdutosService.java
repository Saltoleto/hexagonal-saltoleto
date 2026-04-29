package com.empresa.produto.application.usecase;

import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase;
import com.empresa.produto.domain.port.out.ProdutoRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do caso de uso de listagem de produtos.
 *
 * Responsabilidades:
 * - Orquestrar a chamada ao repositório via port de saída.
 * - Gerenciar a transação (readOnly = true para leitura — evita dirty checking).
 * - Aplicar regras de orquestração se houver (ex: enriquecer resultado, combinar fontes).
 *
 * @Service é legítimo aqui: esta classe pertence à camada application,
 * não ao domínio. O uso de anotações Spring nesta camada é aceito e pragmático.
 */
@Service
@Transactional(readOnly = true)
class ListarProdutosService implements ListarProdutosUseCase {

    private final ProdutoRepositoryPort produtoRepository;

    ListarProdutosService(ProdutoRepositoryPort produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public Page<Produto> executar(Filtro filtro, Pageable pageable) {
        return produtoRepository.listar(filtro, pageable);
    }
}
