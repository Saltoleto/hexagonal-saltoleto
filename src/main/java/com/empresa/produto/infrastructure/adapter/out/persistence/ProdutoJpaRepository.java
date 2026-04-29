package com.empresa.produto.infrastructure.adapter.out.persistence;

import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data JPA Repository.
 *
 * Mantido package-private — nenhum código fora deste pacote deve conhecer esta interface.
 * O ponto de acesso externo é o ProdutoJpaAdapter, que implementa o ProdutoRepositoryPort.
 */
interface ProdutoJpaRepository
        extends JpaRepository<ProdutoEntity, Long>,
                JpaSpecificationExecutor<ProdutoEntity> {
}
