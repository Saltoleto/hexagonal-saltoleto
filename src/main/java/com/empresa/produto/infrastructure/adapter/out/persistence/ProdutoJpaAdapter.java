package com.empresa.produto.infrastructure.adapter.out.persistence;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Pagina.Direcao;
import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.model.ResultadoPaginado;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase.Filtro;
import com.empresa.produto.domain.port.out.ProdutoRepositoryPort;
import com.empresa.produto.infrastructure.adapter.out.persistence.mapper.ProdutoPersistenceMapper;
import com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter de persistência — implementa o port de saída usando Spring Data JPA.
 *
 * É o único lugar da aplicação onde Pageable e Page do Spring existem.
 * Converte Pagina (domínio) → Pageable (Spring) na entrada.
 * Converte Page<ProdutoEntity> → ResultadoPaginado<Produto> na saída.
 *
 * O domínio nunca vê Pageable, Page, Sort ou qualquer tipo do Spring Data.
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
    public ResultadoPaginado<Produto> listar(Filtro filtro, Pagina pagina) {
        var pageable = toPageable(pagina);
        var page = jpaRepository.findAll(ProdutoSpecification.comFiltro(filtro), pageable);

        return new ResultadoPaginado<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.isFirst(),
                page.isLast()
        );
    }

    private Pageable toPageable(Pagina pagina) {
        if (pagina.ordenacoes().isEmpty()) {
            return PageRequest.of(pagina.numero(), pagina.tamanho(), Sort.by("nome").ascending());
        }

        var orders = pagina.ordenacoes().stream()
                .map(o -> o.direcao() == Direcao.DESC
                        ? Sort.Order.desc(o.campo())
                        : Sort.Order.asc(o.campo()))
                .toList();

        return PageRequest.of(pagina.numero(), pagina.tamanho(), Sort.by(orders));
    }
}
