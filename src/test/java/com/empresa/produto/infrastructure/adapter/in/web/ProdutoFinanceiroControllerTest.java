package com.empresa.produto.infrastructure.adapter.in.web;

import com.empresa.produto.domain.model.ResultadoPaginado;
import com.empresa.produto.domain.port.in.ListarProdutosFinanceirosUseCase;
import com.empresa.produto.infrastructure.adapter.in.web.mapper.ProdutoFinanceiroWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdutoFinanceiroController.class)
class ProdutoFinanceiroControllerTest {

    @Autowired MockMvc mvc;
    @MockBean ListarProdutosFinanceirosUseCase listarProdutos;
    @MockBean ProdutoFinanceiroWebMapper mapper;

    @SuppressWarnings("unchecked")
    private ResultadoPaginado resultadoVazio() {
        return new ResultadoPaginado<>(List.of(), 0L, 0, 0, true, true);
    }

    @Test
    void deveRetornar200ComListaVazia() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn(resultadoVazio());

        mvc.perform(get("/api/v1/produtos-financeiros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void deveRetornar400QuandoProdutoIdZero() throws Exception {
        mvc.perform(get("/api/v1/produtos-financeiros/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoUsuarioIdZero() throws Exception {
        mvc.perform(get("/api/v1/produtos-financeiros/1").param("usuarioId", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoNomeExcede150Caracteres() throws Exception {
        mvc.perform(get("/api/v1/produtos-financeiros/1").param("nome", "A".repeat(151)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAceitarOrdenacaoPorSaldoValor() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn(resultadoVazio());

        mvc.perform(get("/api/v1/produtos-financeiros/1").param("sort", "saldo.valor,desc"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAceitarOrdenacaoPorLimiteValor() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn(resultadoVazio());

        mvc.perform(get("/api/v1/produtos-financeiros/1").param("sort", "limite.valor,asc"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAceitarMultiplaOrdenacao() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn(resultadoVazio());

        mvc.perform(get("/api/v1/produtos-financeiros/1")
                        .param("sort", "saldo.valor,desc")
                        .param("sort", "nome,asc"))
                .andExpect(status().isOk());
    }

    @Test
    void deveIgnorarCampoOrdenacaoInvalido() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn(resultadoVazio());

        mvc.perform(get("/api/v1/produtos-financeiros/1").param("sort", "campoInexistente,desc"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAceitarTodosOsFiltros() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn(resultadoVazio());

        mvc.perform(get("/api/v1/produtos-financeiros/42")
                        .param("usuarioId", "7")
                        .param("nome", "cartao")
                        .param("categoria", "ELETRONICO")
                        .param("precoMin", "100")
                        .param("precoMax", "5000")
                        .param("ativo", "true"))
                .andExpect(status().isOk());
    }
}
