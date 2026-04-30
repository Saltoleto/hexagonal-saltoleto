package com.empresa.produto.application.usecase;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.ProdutoFinanceiro;
import com.empresa.produto.domain.model.ProdutoFinanceiroFiltro;
import com.empresa.produto.domain.model.ResultadoPaginado;
import com.empresa.produto.domain.port.in.ListarProdutosFinanceirosUseCase;
import com.empresa.produto.domain.port.out.ProdutoFinanceiroRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ListarProdutosFinanceirosService implements ListarProdutosFinanceirosUseCase {

    private final ProdutoFinanceiroRepositoryPort repository;

    ListarProdutosFinanceirosService(ProdutoFinanceiroRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ResultadoPaginado<ProdutoFinanceiro> executar(ProdutoFinanceiroFiltro filtro, Pagina pagina) {
        return repository.listar(filtro, pagina);
    }
}
