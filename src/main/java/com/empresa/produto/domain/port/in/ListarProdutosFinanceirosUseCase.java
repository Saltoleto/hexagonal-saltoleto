package com.empresa.produto.domain.port.in;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.ProdutoFinanceiro;
import com.empresa.produto.domain.model.ProdutoFinanceiroFiltro;
import com.empresa.produto.domain.model.ResultadoPaginado;

import java.util.Set;

/**
 * Port de entrada — listagem de produtos financeiros.
 *
 * CAMPOS_ORDENACAO_PERMITIDOS inclui campos de ProdutoFinanceiro e
 * de suas relações 1:1 (saldo.valor, limite.valor).
 */
public interface ListarProdutosFinanceirosUseCase {

    Set<String> CAMPOS_ORDENACAO_PERMITIDOS = Set.of(
            "nome", "preco", "estoque", "categoria",
            "saldo.valor", "limite.valor"
    );

    ResultadoPaginado<ProdutoFinanceiro> executar(ProdutoFinanceiroFiltro filtro, Pagina pagina);
}
