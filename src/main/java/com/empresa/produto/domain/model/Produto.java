package com.empresa.produto.domain.model;

import java.math.BigDecimal;

/**
 * Entidade de domínio — núcleo da aplicação.
 *
 * Regras:
 * - Zero anotações de framework (Spring, JPA, Jackson).
 * - Construção controlada via factory method estático.
 * - Comportamento de negócio expresso como métodos, não flags externos.
 * - Sem setters públicos — estado só muda por métodos com intenção clara.
 */
public class Produto {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;
    private CategoriaProduto categoria;
    private boolean ativo;

    private Produto() {}

    /**
     * Reconstitui um produto a partir de dados persistidos.
     * Não valida regras de criação — assume que os dados já foram validados.
     */
    public static Produto reconstituir(
            Long id,
            String nome,
            String descricao,
            BigDecimal preco,
            Integer estoque,
            CategoriaProduto categoria,
            boolean ativo) {

        var produto = new Produto();
        produto.id = id;
        produto.nome = nome;
        produto.descricao = descricao;
        produto.preco = preco;
        produto.estoque = estoque;
        produto.categoria = categoria;
        produto.ativo = ativo;
        return produto;
    }

    // --- Comportamento de negócio ---

    public boolean temEstoque() {
        return this.estoque != null && this.estoque > 0;
    }

    public boolean estaDisponivel() {
        return this.ativo && temEstoque();
    }

    // --- Getters — sem setters públicos ---

    public Long getId()                    { return id; }
    public String getNome()                { return nome; }
    public String getDescricao()           { return descricao; }
    public BigDecimal getPreco()           { return preco; }
    public Integer getEstoque()            { return estoque; }
    public CategoriaProduto getCategoria() { return categoria; }
    public boolean isAtivo()               { return ativo; }
}
