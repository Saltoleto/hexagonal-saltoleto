package com.empresa.produto.infrastructure.adapter.out.persistence;

import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase.Filtro;
import com.empresa.produto.domain.port.out.ProdutoRepositoryPort;
import com.empresa.produto.infrastructure.adapter.out.persistence.mapper.ProdutoPersistenceMapper;
import com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Adapter de persistência — implementa o port de saída usando Spring Data JPA.
 *
 * É o único ponto de contato entre o domínio e o banco de dados.
 * O domínio conhece apenas ProdutoRepositoryPort (interface). Esta classe
 * é invisível para a camada de domínio e application.
 *
 * Responsabilidades:
 * - Delegar a consulta ao JpaRepository com o Specification montado.
 * - Converter ProdutoEntity → Produto (domínio) via mapper.
 * - Nunca expor ProdutoEntity para fora deste pacote.
 */
@Component
class ProdutoJpaAdapter implements ProdutoRepositoryPort {

    private final ProdutoJpaRepository jpaRepository;
    private final ProdutoPersistenceMapper mapper;

    ProdutoJpaAdapter(ProdutoJpaRepository jpaRepository, ProdutoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<Produto> listar(Filtro filtro, Pageable pageable) {
        return jpaRepository
                .findAll(ProdutoSpecification.comFiltro(filtro), pageable)
                .map(mapper::toDomain);
    }
}
