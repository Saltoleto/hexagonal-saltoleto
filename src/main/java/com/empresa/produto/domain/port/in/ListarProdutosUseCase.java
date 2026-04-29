package com.empresa.produto.domain.port.in;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Produto;
import com.empresa.produto.domain.model.ResultadoPaginado;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Port de entrada — contrato do caso de uso de listagem de produtos.
 *
 * CAMPOS_ORDENACAO_PERMITIDOS é uma regra do domínio: define quais atributos
 * de Produto podem ser usados como critério de ordenação. Vive aqui para que
 * qualquer adapter de entrada (HTTP, gRPC, mensageria) referencie a mesma
 * fonte de verdade sem duplicar esse conhecimento.
 */
public interface ListarProdutosUseCase {

    Set<String> CAMPOS_ORDENACAO_PERMITIDOS = Set.of("nome", "preco", "estoque", "categoria");

    record Filtro(
            Long produtoId,
            Long usuarioId,
            String nome,
            String categoria,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Boolean ativo
    ) {}

    ResultadoPaginado<Produto> executar(Filtro filtro, Pagina pagina);
}
