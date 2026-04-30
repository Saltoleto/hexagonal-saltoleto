package com.empresa.produto.infrastructure.adapter.out.persistence.specification;

import com.empresa.produto.domain.model.ProdutoFinanceiroFiltro;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoFinanceiroEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Specification para filtros dinâmicos de ProdutoFinanceiro.
 *
 * Cada filtro é um método privado que retorna null quando inativo —
 * o Spring Data ignora specs nulas em Specification.where().and().
 *
 * O JOIN com saldo e limite é responsabilidade do @EntityGraph no repository,
 * não desta classe. Aqui ficam apenas os predicados de filtragem.
 */
public final class ProdutoFinanceiroSpecification {

    private ProdutoFinanceiroSpecification() {}

    public static Specification<ProdutoFinanceiroEntity> comFiltro(ProdutoFinanceiroFiltro filtro) {
        return Specification
                .where(comProdutoId(filtro.produtoId()))
                .and(comUsuarioId(filtro.usuarioId()))
                .and(comNome(filtro.nome()))
                .and(comCategoria(filtro.categoria()))
                .and(comPrecoMinimo(filtro.precoMin()))
                .and(comPrecoMaximo(filtro.precoMax()))
                .and(comAtivo(filtro.ativo()));
    }

    private static Specification<ProdutoFinanceiroEntity> comProdutoId(Long produtoId) {
        return (root, query, cb) ->
                produtoId == null ? null
                : cb.equal(root.get("id"), produtoId);
    }

    private static Specification<ProdutoFinanceiroEntity> comUsuarioId(Long usuarioId) {
        return (root, query, cb) ->
                usuarioId == null ? null
                : cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<ProdutoFinanceiroEntity> comNome(String nome) {
        return (root, query, cb) ->
                (nome == null || nome.isBlank()) ? null
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    private static Specification<ProdutoFinanceiroEntity> comCategoria(String categoria) {
        return (root, query, cb) ->
                (categoria == null || categoria.isBlank()) ? null
                : cb.equal(root.get("categoria"), categoria.toUpperCase());
    }

    private static Specification<ProdutoFinanceiroEntity> comPrecoMinimo(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? null
                : cb.greaterThanOrEqualTo(root.get("preco"), min);
    }

    private static Specification<ProdutoFinanceiroEntity> comPrecoMaximo(BigDecimal max) {
        return (root, query, cb) ->
                max == null ? null
                : cb.lessThanOrEqualTo(root.get("preco"), max);
    }

    private static Specification<ProdutoFinanceiroEntity> comAtivo(Boolean ativo) {
        return (root, query, cb) ->
                ativo == null ? null
                : cb.equal(root.get("ativo"), ativo);
    }
}
