package com.empresa.produto.infrastructure.adapter.out.persistence;

import com.empresa.produto.domain.port.in.ListarProdutosUseCase.Filtro;
import com.empresa.produto.infrastructure.adapter.out.persistence.entity.ProdutoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slice test de persistência — valida queries e specifications contra H2.
 * Não levanta contexto web. Flyway desabilitado no profile test.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProdutoJpaRepositoryTest {

    @Autowired
    ProdutoJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(entidade("Notebook Dell", "ELETRONICO", new BigDecimal("3500"), 10, true));
        repository.save(entidade("Smartphone Samsung", "ELETRONICO", new BigDecimal("1800"), 5, true));
        repository.save(entidade("Camiseta Polo", "VESTUARIO", new BigDecimal("90"), 100, true));
        repository.save(entidade("Fone Bluetooth", "ELETRONICO", new BigDecimal("300"), 0, false));
    }

    @Test
    void deveListarTodosSemFiltro() {
        var filtro = new Filtro(null, null, null, null, null);
        var resultado = repository.findAll(
                com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(4, resultado.getTotalElements());
    }

    @Test
    void deveFiltrarPorNomeParcial() {
        var filtro = new Filtro("note", null, null, null, null);
        var resultado = repository.findAll(
                com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Notebook Dell", resultado.getContent().get(0).getNome());
    }

    @Test
    void deveFiltrarPorCategoriaEAtivo() {
        var filtro = new Filtro(null, "ELETRONICO", null, null, true);
        var resultado = repository.findAll(
                com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    void deveFiltrarPorFaixaDePreco() {
        var filtro = new Filtro(null, null, new BigDecimal("1000"), new BigDecimal("4000"), null);
        var resultado = repository.findAll(
                com.empresa.produto.infrastructure.adapter.out.persistence.specification.ProdutoSpecification.comFiltro(filtro),
                PageRequest.of(0, 10)
        );
        assertEquals(2, resultado.getTotalElements());
    }

    private ProdutoEntity entidade(String nome, String categoria, BigDecimal preco, int estoque, boolean ativo) {
        // Usando reflection para instanciar a entity package-private
        try {
            var constructor = ProdutoEntity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var e = constructor.newInstance();

            var setNome = ProdutoEntity.class.getDeclaredMethod("setNome", String.class);
            setNome.setAccessible(true); setNome.invoke(e, nome);

            var setCategoria = ProdutoEntity.class.getDeclaredMethod("setCategoria", String.class);
            setCategoria.setAccessible(true); setCategoria.invoke(e, categoria);

            var setPreco = ProdutoEntity.class.getDeclaredMethod("setPreco", java.math.BigDecimal.class);
            setPreco.setAccessible(true); setPreco.invoke(e, preco);

            var setEstoque = ProdutoEntity.class.getDeclaredMethod("setEstoque", Integer.class);
            setEstoque.setAccessible(true); setEstoque.invoke(e, estoque);

            var setAtivo = ProdutoEntity.class.getDeclaredMethod("setAtivo", boolean.class);
            setAtivo.setAccessible(true); setAtivo.invoke(e, ativo);

            return e;
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao criar entidade de teste", ex);
        }
    }
}
