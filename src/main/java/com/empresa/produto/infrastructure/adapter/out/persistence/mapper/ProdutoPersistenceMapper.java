package com.empresa.produto.infrastructure.adapter.out.persistence.mapper;

import com.empresa.produto.domain.model.CategoriaProduto;
import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de persistência — converte entre ProdutoEntity (infraestrutura) e Produto (domínio).
 *
 * Cada camada tem seu próprio mapper. Este mapper existe exclusivamente
 * para cruzar a fronteira persistence → domain.
 *
 * O campo `categoria` é armazenado como String no banco e convertido
 * para enum no domínio via expression explícita.
 */
@Mapper(componentModel = "spring")
public interface ProdutoPersistenceMapper {

    @Mapping(
        target = "categoria",
        expression = "java(entity.getCategoria() != null ? com.empresa.produto.domain.model.CategoriaProduto.valueOf(entity.getCategoria()) : null)"
    )
    Produto toDomain(ProdutoEntity entity);
}
