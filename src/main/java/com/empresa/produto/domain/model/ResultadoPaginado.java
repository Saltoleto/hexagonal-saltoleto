package com.empresa.produto.domain.model;

import java.util.List;
import java.util.function.Function;

/**
 * Resultado paginado — tipo próprio do domínio.
 *
 * Substitui o Page<T> do Spring nas camadas domain e application.
 * O adapter de persistência converte Page<Entity> → ResultadoPaginado<Produto>.
 * O controller converte ResultadoPaginado<Produto> → Page<ProdutoResponse> para a resposta HTTP.
 */
public record ResultadoPaginado<T>(
        List<T> conteudo,
        long totalElementos,
        int totalPaginas,
        int paginaAtual,
        boolean primeira,
        boolean ultima
) {

    public <R> ResultadoPaginado<R> map(Function<T, R> mapper) {
        return new ResultadoPaginado<>(
                conteudo.stream().map(mapper).toList(),
                totalElementos,
                totalPaginas,
                paginaAtual,
                primeira,
                ultima
        );
    }
}
