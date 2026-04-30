package com.empresa.produto.domain;

import com.empresa.produto.domain.model.CategoriaProduto;
import com.empresa.produto.domain.model.DomainException;
import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.ProdutoFinanceiro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoFinanceiroTest {

    private ProdutoFinanceiro produto(int estoque, boolean ativo) {
        return ProdutoFinanceiro.reconstituir(
                1L, "Produto Teste", "Descrição",
                new BigDecimal("100.00"), estoque,
                CategoriaProduto.ELETRONICO, ativo, null, null, null
        );
    }

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
    void naoDeveEstarDisponivelSemEstoque() {
        assertFalse(produto(0, true).estaDisponivel());
    }

    @Test
    void deveTerSaldoELimiteNullQuandoNaoCadastrado() {
        var p = produto(5, true);
        assertNull(p.getSaldo());
        assertNull(p.getLimite());
    }

    @Test
    void deveCriarPaginaValida() {
        var pagina = Pagina.de(0, 20);
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
