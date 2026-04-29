package com.empresa.produto.infrastructure.adapter.out.persistence.specification;

import com.empresa.produto.domain.port.in.ListarProdutosUseCase.Filtro;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Specification para filtros dinâmicos de produto.
 *
 * Cada filtro é um método privado independente que retorna null quando inativo.
 * O Spring Data ignora specs nulas em Specification.where().and() — sem predicate
 * desnecessário na query.
 *
 * Adicionar um novo filtro = adicionar um método privado + uma linha no comFiltro().
 */
public final class ProdutoSpecification {

    private ProdutoSpecification() {}

    public static Specification<ProdutoEntity> comFiltro(Filtro filtro) {
        return Specification
                .where(comProdutoId(filtro.produtoId()))
                .and(comUsuarioId(filtro.usuarioId()))
                .and(comNome(filtro.nome()))
                .and(comCategoria(filtro.categoria()))
                .and(comPrecoMinimo(filtro.precoMin()))
                .and(comPrecoMaximo(filtro.precoMax()))
                .and(comAtivo(filtro.ativo()));
    }

    private static Specification<ProdutoEntity> comProdutoId(Long produtoId) {
        return (root, query, cb) ->
                produtoId == null ? null
                : cb.equal(root.get("id"), produtoId);
    }

    private static Specification<ProdutoEntity> comUsuarioId(Long usuarioId) {
        return (root, query, cb) ->
                usuarioId == null ? null
                : cb.equal(root.get("usuarioId"), usuarioId);
    }

    private static Specification<ProdutoEntity> comNome(String nome) {
        return (root, query, cb) ->
                (nome == null || nome.isBlank()) ? null
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    private static Specification<ProdutoEntity> comCategoria(String categoria) {
        return (root, query, cb) ->
                (categoria == null || categoria.isBlank()) ? null
                : cb.equal(root.get("categoria"), categoria.toUpperCase());
    }

    private static Specification<ProdutoEntity> comPrecoMinimo(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? null
                : cb.greaterThanOrEqualTo(root.get("preco"), min);
    }

    private static Specification<ProdutoEntity> comPrecoMaximo(BigDecimal max) {
        return (root, query, cb) ->
                max == null ? null
                : cb.lessThanOrEqualTo(root.get("preco"), max);
    }

    private static Specification<ProdutoEntity> comAtivo(Boolean ativo) {
        return (root, query, cb) ->
                ativo == null ? null
                : cb.equal(root.get("ativo"), ativo);
    }
}
