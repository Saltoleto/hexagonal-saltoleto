package com.empresa.produto.infrastructure.adapter.out.persistence;

import com.empresa.produto.domain.model.ProdutoFinanceiroFiltro;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoFinanceiroEntity;
import com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoFinanceiroSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProdutoFinanceiroJpaRepositoryTest {

    @Autowired
    ProdutoFinanceiroJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(entidade("Cartão Gold",    "ELETRONICO", new BigDecimal("3500"), 10, true));
        repository.save(entidade("Cartão Platinum","ELETRONICO", new BigDecimal("1800"),  5, true));
        repository.save(entidade("Conta Digital",  "OUTRO",      new BigDecimal("90"),  100, true));
        repository.save(entidade("Cartão Black",   "ELETRONICO", new BigDecimal("300"),   0, false));
    }

    @Test
    void deveListarTodosSemFiltro() {
        var filtro = new ProdutoFinanceiroFiltro(null, null, null, null, null, null, null);
        var resultado = repository.findAll(
                ProdutoFinanceiroSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(4, resultado.getTotalElements());
    }

    @Test
    void deveFiltrarPorNomeParcial() {
        var filtro = new ProdutoFinanceiroFiltro(null, null, "cartão", null, null, null, null);
        var resultado = repository.findAll(
                ProdutoFinanceiroSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(3, resultado.getTotalElements());
    }

    @Test
    void deveFiltrarPorCategoriaEAtivo() {
        var filtro = new ProdutoFinanceiroFiltro(null, null, null, "ELETRONICO", null, null, true);
        var resultado = repository.findAll(
                ProdutoFinanceiroSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    void deveFiltrarPorFaixaDePreco() {
        var filtro = new ProdutoFinanceiroFiltro(null, null, null, null, new BigDecimal("1000"), new BigDecimal("4000"), null);
        var resultado = repository.findAll(
                ProdutoFinanceiroSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(2, resultado.getTotalElements());
    }

    private ProdutoFinanceiroEntity entidade(String nome, String categoria, BigDecimal preco, int estoque, boolean ativo) {
        try {
            var constructor = ProdutoFinanceiroEntity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var e = constructor.newInstance();
            set(e, "setNome",      String.class,     nome);
            set(e, "setCategoria", String.class,     categoria);
            set(e, "setPreco",     BigDecimal.class, preco);
            set(e, "setEstoque",   Integer.class,    estoque);
            set(e, "setAtivo",     boolean.class,    ativo);
            return e;
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao criar entidade de teste", ex);
        }
    }

    private void set(Object obj, String method, Class<?> type, Object value) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(method, type);
        m.setAccessible(true);
        m.invoke(obj, value);
    }
}
