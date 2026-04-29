package com.empresa.produto.domain;

import com.empresa.produto.domain.model.CategoriaProduto;
import com.empresa.produto.domain.model.DomainException;
import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de domínio — zero Spring, zero banco, zero I/O.
 */
class ProdutoTest {

    private Produto produto(int estoque, boolean ativo) {
        return Produto.reconstituir(1L, "Produto Teste", "Descrição",
                new BigDecimal("100.00"), estoque, CategoriaProduto.ELETRONICO, ativo);
    }

    // --- Produto ---

    @Test
    void deveReconhecerProdutoComEstoque() {
        assertTrue(produto(10, true).temEstoque());
    }

    @Test
    void deveReconhecerProdutoSemEstoque() {
        assertFalse(produto(0, true).temEstoque());
    }

    @Test
    void deveEstarDisponivelQuandoAtivoEComEstoque() {
        assertTrue(produto(5, true).estaDisponivel());
    }

    @Test
    void naoDeveEstarDisponivelQuandoInativo() {
        assertFalse(produto(10, false).estaDisponivel());
    }

    @Test
    void naoDeveEstarDisponivelQuandoSemEstoqueMesmoAtivo() {
        assertFalse(produto(0, true).estaDisponivel());
    }

    // --- Pagina ---

    @Test
    void deveCriarPaginaValida() {
        var pagina = Pagina.de(0, 20, List.of());
        assertEquals(0, pagina.numero());
        assertEquals(20, pagina.tamanho());
    }

    @Test
    void deveRejeitarNumeroNegativo() {
        assertThrows(DomainException.class, () -> Pagina.de(-1, 20));
    }

    @Test
    void deveRejeitarTamanhoZero() {
        assertThrows(DomainException.class, () -> Pagina.de(0, 0));
    }
}
