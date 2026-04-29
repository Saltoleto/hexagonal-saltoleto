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
 * Responsabilidades:
 * - ProdutoFiltroRequest → Filtro (entrada do use case)
 * - Produto (domínio) → ProdutoResponse (saída HTTP)
 *
 * Campos computados (temEstoque, estaDisponivel) são mapeados via expression,
 * delegando para os métodos do próprio domínio — sem duplicar lógica aqui.
 */
@Mapper(componentModel = "spring")
public interface ProdutoWebMapper {

    @Mapping(target = "categoria", source = "categoria")
    Filtro toFiltro(ProdutoFiltroRequest request);

    @Mapping(
        target = "categoria",
        expression = "java(produto.getCategoria() != null ? produto.getCategoria().name() : null)"
    )
    @Mapping(target = "temEstoque",      expression = "java(produto.temEstoque())")
    @Mapping(target = "estaDisponivel",  expression = "java(produto.estaDisponivel())")
    ProdutoResponse toResponse(Produto produto);
}
