package com.empresa.produto.infrastructure.adapter.in.web.mapper;

import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase.Filtro;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFiltroRequest;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da camada web — converte entre DTOs HTTP e objetos de domínio.
 *
 * toFiltro recebe produtoId e usuarioId separadamente pois chegam por
 * mecanismos HTTP distintos (@PathVariable e @RequestParam) — não fazem
 * parte do ProdutoFiltroRequest, que só carrega query params de filtro.
 *
 * Campos computados (temEstoque, estaDisponivel) delegam para métodos
 * do domínio — sem duplicar lógica no mapper.
 */
@Mapper(componentModel = "spring")
public interface ProdutoWebMapper {

    @Mapping(target = "produtoId",  source = "produtoId")
    @Mapping(target = "usuarioId",  source = "usuarioId")
    @Mapping(target = "nome",       source = "request.nome")
    @Mapping(target = "categoria",  source = "request.categoria")
    @Mapping(target = "precoMin",   source = "request.precoMin")
    @Mapping(target = "precoMax",   source = "request.precoMax")
    @Mapping(target = "ativo",      source = "request.ativo")
    Filtro toFiltro(Long produtoId, Long usuarioId, ProdutoFiltroRequest request);

    @Mapping(
        target = "categoria",
        expression = "java(produto.getCategoria() != null ? produto.getCategoria().name() : null)"
    )
    @Mapping(target = "temEstoque",     expression = "java(produto.temEstoque())")
    @Mapping(target = "estaDisponivel", expression = "java(produto.estaDisponivel())")
    ProdutoResponse toResponse(Produto produto);
}
