package com.empresa.produto.infrastructure.adapter.out.persistence;

import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoFinanceiroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface ProdutoFinanceiroJpaRepository
        extends JpaRepository<ProdutoFinanceiroEntity, Long>,
                JpaSpecificationExecutor<ProdutoFinanceiroEntity> {

    /**
     * Sempre carrega saldo e limite via LEFT JOIN FETCH — sem N+1.
     * O JOIN já presente permite ordenar por saldo.valor e limite.valor
     * sem nenhuma lógica adicional.
     */
    @EntityGraph(attributePaths = {"saldo", "limite"})
    Page<ProdutoFinanceiroEntity> findAll(Specification<ProdutoFinanceiroEntity> spec, Pageable pageable);
}
