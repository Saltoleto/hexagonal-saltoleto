package com.empresa.produto.domain.port.out;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.ProdutoFinanceiro;
import com.empresa.produto.domain.model.ProdutoFinanceiroFiltro;
import com.empresa.produto.domain.model.ResultadoPaginado;

/**
 * Port de saída — contrato de persistência para produtos financeiros.
 */
public interface ProdutoFinanceiroRepositoryPort {
    ResultadoPaginado<ProdutoFinanceiro> listar(ProdutoFinanceiroFiltro filtro, Pagina pagina);
}
