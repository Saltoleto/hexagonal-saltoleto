package com.empresa.produto.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidade JPA — tabela `produtos_financeiros`.
 * Agrega SaldoEntity e LimiteEntity via @OneToOne com LEFT JOIN (opcional).
 */
@Entity
@Table(name = "produtos_financeiros")
class ProdutoFinanceiroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @OneToOne(mappedBy = "produtoFinanceiro", fetch = FetchType.LAZY)
    private SaldoEntity saldo;

    @OneToOne(mappedBy = "produtoFinanceiro", fetch = FetchType.LAZY)
    private LimiteEntity limite;

    ProdutoFinanceiroEntity() {}

    Long getId()                  { return id; }
    String getNome()              { return nome; }
    String getDescricao()         { return descricao; }
    BigDecimal getPreco()         { return preco; }
    Integer getEstoque()          { return estoque; }
    String getCategoria()         { return categoria; }
    boolean isAtivo()             { return ativo; }
    Long getUsuarioId()           { return usuarioId; }
    SaldoEntity getSaldo()        { return saldo; }
    LimiteEntity getLimite()      { return limite; }

    void setId(Long id)                         { this.id = id; }
    void setNome(String nome)                   { this.nome = nome; }
    void setDescricao(String descricao)         { this.descricao = descricao; }
    void setPreco(BigDecimal preco)             { this.preco = preco; }
    void setEstoque(Integer estoque)            { this.estoque = estoque; }
    void setCategoria(String categoria)         { this.categoria = categoria; }
    void setAtivo(boolean ativo)               { this.ativo = ativo; }
    void setUsuarioId(Long usuarioId)           { this.usuarioId = usuarioId; }
}
