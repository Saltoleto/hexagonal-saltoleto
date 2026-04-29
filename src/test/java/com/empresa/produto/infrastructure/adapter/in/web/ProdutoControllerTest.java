package com.empresa.produto.infrastructure.adapter.in.web;

import com.empresa.produto.domain.model.ResultadoPaginado;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase;
import com.empresa.produto.infrastructure.adapter.in.web.mapper.ProdutoWebMapper;
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

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ListarProdutosUseCase listarProdutos;

    @MockBean
    ProdutoWebMapper mapper;

    private ResultadoPaginado<Object> resultadoVazio() {
        return new ResultadoPaginado<>(List.of(), 0L, 0, 0, true, true);
    }

    @Test
    void deveRetornar200ComListaVazia() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn((ResultadoPaginado) resultadoVazio());

        mvc.perform(get("/api/v1/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void deveRetornar400QuandoNomeExcede150Caracteres() throws Exception {
        mvc.perform(get("/api/v1/produtos").param("nome", "A".repeat(151)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoPrecoMinNegativo() throws Exception {
        mvc.perform(get("/api/v1/produtos").param("precoMin", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAceitarFiltrosValidos() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn((ResultadoPaginado) resultadoVazio());

        mvc.perform(get("/api/v1/produtos")
                        .param("nome", "notebook")
                        .param("categoria", "ELETRONICO")
                        .param("precoMin", "500")
                        .param("precoMax", "5000")
                        .param("ativo", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAceitarOrdenacaoPorCampoValido() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn((ResultadoPaginado) resultadoVazio());

        mvc.perform(get("/api/v1/produtos").param("sort", "preco,desc"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAceitarOrdenacaoPorMultiplosCampos() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn((ResultadoPaginado) resultadoVazio());

        mvc.perform(get("/api/v1/produtos")
                        .param("sort", "categoria,asc")
                        .param("sort", "preco,desc"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar200MesmoCampoOrdenacaoInvalido() throws Exception {
        when(listarProdutos.executar(any(), any())).thenReturn((ResultadoPaginado) resultadoVazio());

        mvc.perform(get("/api/v1/produtos").param("sort", "campoInexistente,desc"))
                .andExpect(status().isOk());
    }
}
