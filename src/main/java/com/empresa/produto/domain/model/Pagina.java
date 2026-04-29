package com.empresa.produto.domain.model;

import java.util.List;

/**
 * Parâmetros de paginação e ordenação — tipo próprio do domínio.
 *
 * Substitui o Pageable do Spring nas camadas domain e application.
 * A conversão para Pageable acontece exclusivamente no adapter de persistência.
 */
public record Pagina(
        int numero,
        int tamanho,
        List<Ordenacao> ordenacoes
) {

    public record Ordenacao(String campo, Direcao direcao) {}

    public enum Direcao { ASC, DESC }

    public static Pagina de(int numero, int tamanho, List<Ordenacao> ordenacoes) {
        if (numero < 0) throw new DomainException("Número da página não pode ser negativo");
        if (tamanho < 1) throw new DomainException("Tamanho da página deve ser maior que zero");
        return new Pagina(numero, tamanho, ordenacoes == null ? List.of() : ordenacoes);
    }

    public static Pagina de(int numero, int tamanho) {
        return de(numero, tamanho, List.of());
    }
}
