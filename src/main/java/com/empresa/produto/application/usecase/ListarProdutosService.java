package com.empresa.produto.application.usecase;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.model.ResultadoPaginado;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase;
import com.empresa.produto.domain.port.out.ProdutoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação do caso de uso de listagem de produtos.
 *
 * Depende apenas de tipos do domínio — Pagina e ResultadoPaginado.
 * Zero importação de Spring Data nesta camada.
 *
 * @Service e @Transactional são as únicas anotações Spring permitidas aqui:
 * gerenciam ciclo de vida e transação, não lógica de negócio.
 */
@Service
@Transactional(readOnly = true)
class ListarProdutosService implements ListarProdutosUseCase {

    private final ProdutoRepositoryPort produtoRepository;

    ListarProdutosService(ProdutoRepositoryPort produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public ResultadoPaginado<Produto> executar(Filtro filtro, Pagina pagina) {
        return produtoRepository.listar(filtro, pagina);
    }
}
