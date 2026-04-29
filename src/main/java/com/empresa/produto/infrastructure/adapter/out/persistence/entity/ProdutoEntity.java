package com.empresa.produto.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidade JPA — representação da tabela `produtos` no banco de dados.
 *
 * Confinada à camada de infraestrutura. Nunca exposta para fora do adapter de persistência.
 * Separada do modelo de domínio para que mudanças no schema não afetem o domínio e vice-versa.
 */
@Entity
@Table(name = "produtos")
class ProdutoEntity {

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

    // Construtor padrão exigido pelo JPA
    ProdutoEntity() {}

    Long getId()               { return id; }
    String getNome()           { return nome; }
    String getDescricao()      { return descricao; }
    BigDecimal getPreco()      { return preco; }
    Integer getEstoque()       { return estoque; }
    String getCategoria()      { return categoria; }
    boolean isAtivo()          { return ativo; }

    void setId(Long id)                   { this.id = id; }
    void setNome(String nome)             { this.nome = nome; }
    void setDescricao(String descricao)   { this.descricao = descricao; }
    void setPreco(BigDecimal preco)       { this.preco = preco; }
    void setEstoque(Integer estoque)      { this.estoque = estoque; }
    void setCategoria(String categoria)   { this.categoria = categoria; }
    void setAtivo(boolean ativo)          { this.ativo = ativo; }
}
