package com.empresa.produto.infrastructure.adapter.out.persistence;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Pagina.Direcao;
import com.empresa.produto.domain.model.ProdutoFinanceiro;
import com.empresa.produto.domain.model.ProdutoFinanceiroFiltro;
import com.empresa.produto.domain.model.ResultadoPaginado;
import com.empresa.produto.domain.port.out.ProdutoFinanceiroRepositoryPort;
import com.empresa.produto.infrastructure.adapter.out.persistence.mapper.ProdutoFinanceiroPersistenceMapper;
import com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoFinanceiroSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Adapter de persistência — implementa ProdutoFinanceiroRepositoryPort.
 *
 * Saldo e limite são sempre carregados via @EntityGraph no repository —
 * um único LEFT JOIN FETCH, sem N+1.
 *
 * A ordenação por saldo.valor e limite.valor funciona naturalmente
 * porque o JOIN já está presente em todas as queries.
 */
@Component
class ProdutoFinanceiroJpaAdapter implements ProdutoFinanceiroRepositoryPort {

    private final ProdutoFinanceiroJpaRepository jpaRepository;
    private final ProdutoFinanceiroPersistenceMapper mapper;

    ProdutoFinanceiroJpaAdapter(ProdutoFinanceiroJpaRepository jpaRepository,
                                ProdutoFinanceiroPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ResultadoPaginado<ProdutoFinanceiro> listar(ProdutoFinanceiroFiltro filtro, Pagina pagina) {
        var page = jpaRepository.findAll(
                ProdutoFinanceiroSpecification.comFiltro(filtro),
                toPageable(pagina)
        );

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
