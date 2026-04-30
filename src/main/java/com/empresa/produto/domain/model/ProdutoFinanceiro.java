package com.empresa.produto.domain.model;

import java.math.BigDecimal;

/**
 * Entidade de domínio — produto financeiro.
 *
 * Agrega Saldo e Limite como objetos de valor opcionais (1:1).
 * Quando não cadastrados, retornam null — nunca lançam exceção por ausência.
 */
public class ProdutoFinanceiro {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;
    private CategoriaProduto categoria;
    private boolean ativo;
    private Long usuarioId;
    private Saldo saldo;
    private Limite limite;

    private ProdutoFinanceiro() {}

    public static ProdutoFinanceiro reconstituir(
            Long id,
            String nome,
            String descricao,
            BigDecimal preco,
            Integer estoque,
            CategoriaProduto categoria,
            boolean ativo,
            Long usuarioId,
            Saldo saldo,
            Limite limite) {

        var p = new ProdutoFinanceiro();
        p.id        = id;
        p.nome      = nome;
        p.descricao = descricao;
        p.preco     = preco;
        p.estoque   = estoque;
        p.categoria = categoria;
        p.ativo     = ativo;
        p.usuarioId = usuarioId;
        p.saldo     = saldo;
        p.limite    = limite;
        return p;
    }

    public boolean temEstoque()     { return estoque != null && estoque > 0; }
    public boolean estaDisponivel() { return ativo && temEstoque(); }

    public Long getId()                    { return id; }
    public String getNome()                { return nome; }
    public String getDescricao()           { return descricao; }
    public BigDecimal getPreco()           { return preco; }
    public Integer getEstoque()            { return estoque; }
    public CategoriaProduto getCategoria() { return categoria; }
    public boolean isAtivo()               { return ativo; }
    public Long getUsuarioId()             { return usuarioId; }
    public Saldo getSaldo()                { return saldo; }
    public Limite getLimite()              { return limite; }
}
