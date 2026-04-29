package com.empresa.produto.domain;

import com.empresa.produto.domain.model.CategoriaProduto;
import com.empresa.produto.domain.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de domínio — zero Spring, zero banco, zero I/O.
 * Devem ser rápidos, determinísticos e expressar regras de negócio.
 */
class ProdutoTest {

    private Produto produto(int estoque, boolean ativo) {
        return Produto.reconstituir(
                1L,
                "Produto Teste",
                "Descrição",
                new BigDecimal("100.00"),
                estoque,
                CategoriaProduto.ELETRONICO,
                ativo
        );
    }

    @Test
    void deveReconhecerProdutoComEstoque() {
        var produto = produto(10, true);
        assertTrue(produto.temEstoque());
    }

    @Test
    void deveReconhecerProdutoSemEstoque() {
        var produto = produto(0, true);
        assertFalse(produto.temEstoque());
    }

    @Test
    void deveEstarDisponivelQuandoAtivoEComEstoque() {
        var produto = produto(5, true);
        assertTrue(produto.estaDisponivel());
    }

    @Test
    void naoDeveEstarDisponivelQuandoInativo() {
        var produto = produto(10, false);
        assertFalse(produto.estaDisponivel());
    }

    @Test
    void naoDeveEstarDisponivelQuandoSemEstoqueMesmoAtivo() {
        var produto = produto(0, true);
        assertFalse(produto.estaDisponivel());
    }
}
