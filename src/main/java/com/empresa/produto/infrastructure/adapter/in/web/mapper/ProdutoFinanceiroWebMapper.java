package com.empresa.produto.infrastructure.adapter.in.web.mapper;

import com.empresa.produto.domain.model.Limite;
import com.empresa.produto.domain.model.ProdutoFinanceiro;
import com.empresa.produto.domain.model.ProdutoFinanceiroFiltro;
import com.empresa.produto.domain.model.Saldo;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFinanceiroFiltroRequest;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFinanceiroResponse;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFinanceiroResponse.LimiteResponse;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFinanceiroResponse.SaldoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProdutoFinanceiroWebMapper {

    @Mapping(target = "produtoId", source = "produtoId")
    @Mapping(target = "usuarioId", source = "usuarioId")
    @Mapping(target = "nome",      source = "request.nome")
    @Mapping(target = "categoria", source = "request.categoria")
    @Mapping(target = "precoMin",  source = "request.precoMin")
    @Mapping(target = "precoMax",  source = "request.precoMax")
    @Mapping(target = "ativo",     source = "request.ativo")
    ProdutoFinanceiroFiltro toFiltro(Long produtoId, Long usuarioId, ProdutoFinanceiroFiltroRequest request);

    @Mapping(
        target = "categoria",
        expression = "java(produto.getCategoria() != null ? produto.getCategoria().name() : null)"
    )
    @Mapping(target = "temEstoque",     expression = "java(produto.temEstoque())")
    @Mapping(target = "estaDisponivel", expression = "java(produto.estaDisponivel())")
    @Mapping(target = "saldo",          expression = "java(toSaldoResponse(produto.getSaldo()))")
    @Mapping(target = "limite",         expression = "java(toLimiteResponse(produto.getLimite()))")
    ProdutoFinanceiroResponse toResponse(ProdutoFinanceiro produto);

    default SaldoResponse toSaldoResponse(Saldo saldo) {
        return saldo == null ? null : new SaldoResponse(saldo.id(), saldo.valor());
    }

    default LimiteResponse toLimiteResponse(Limite limite) {
        return limite == null ? null : new LimiteResponse(limite.id(), limite.valor());
    }
}
