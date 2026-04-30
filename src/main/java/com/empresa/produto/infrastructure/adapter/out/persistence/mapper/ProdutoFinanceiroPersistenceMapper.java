package com.empresa.produto.infrastructure.adapter.out.persistence.mapper;

import com.empresa.produto.domain.model.CategoriaProduto;
import com.empresa.produto.domain.model.Limite;
import com.empresa.produto.domain.model.ProdutoFinanceiro;
import com.empresa.produto.domain.model.Saldo;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.LimiteEntity;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoFinanceiroEntity;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.SaldoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de persistência — converte ProdutoFinanceiroEntity (+ SaldoEntity + LimiteEntity)
 * para o modelo de domínio ProdutoFinanceiro.
 */
@Mapper(componentModel = "spring")
public interface ProdutoFinanceiroPersistenceMapper {

    @Mapping(
        target = "categoria",
        expression = "java(entity.getCategoria() != null ? com.empresa.produto.domain.model.CategoriaProduto.valueOf(entity.getCategoria()) : null)"
    )
    @Mapping(target = "saldo",  expression = "java(toSaldo(entity.getSaldo()))")
    @Mapping(target = "limite", expression = "java(toLimite(entity.getLimite()))")
    ProdutoFinanceiro toDomain(ProdutoFinanceiroEntity entity);

    default Saldo toSaldo(SaldoEntity entity) {
        if (entity == null) return null;
        return new Saldo(entity.getId(), entity.getValor(), entity.getProdutoFinanceiro().getId());
    }

    default Limite toLimite(LimiteEntity entity) {
        if (entity == null) return null;
        return new Limite(entity.getId(), entity.getValor(), entity.getProdutoFinanceiro().getId());
    }
}
