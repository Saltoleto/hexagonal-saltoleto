package com.empresa.produto.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidade JPA — tabela `limites`.
 * FK para ProdutoFinanceiro (1:1).
 */
@Entity
@Table(name = "limites")
class LimiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_financeiro_id", nullable = false)
    private ProdutoFinanceiroEntity produtoFinanceiro;

    LimiteEntity() {}

    Long getId()                                    { return id; }
    BigDecimal getValor()                           { return valor; }
    ProdutoFinanceiroEntity getProdutoFinanceiro()  { return produtoFinanceiro; }

    void setId(Long id)                                          { this.id = id; }
    void setValor(BigDecimal valor)                              { this.valor = valor; }
    void setProdutoFinanceiro(ProdutoFinanceiroEntity pf)        { this.produtoFinanceiro = pf; }
}
