package com.empresa.produto.infrastructure.adapter.in.web;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Pagina.Direcao;
import com.empresa.produto.domain.model.Pagina.Ordenacao;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFiltroRequest;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoResponse;
import com.empresa.produto.infrastructure.adapter.in.web.mapper.ProdutoWebMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Adapter de entrada HTTP — traduz requisições REST em chamadas ao use case.
 *
 * Fronteira de tradução de paginação:
 * - Entrada:  Pageable (Spring) → Pagina (domínio)
 * - Saída:    ResultadoPaginado (domínio) → Page<ProdutoResponse> (Spring/HTTP)
 *
 * Pageable e Page do Spring ficam confinados nesta classe.
 * O use case opera exclusivamente com tipos do domínio.
 *
 * Ordenação:
 * - Campos permitidos: nome, preco, estoque, categoria
 * - Campos inválidos são ignorados — fallback: nome, ASC
 * - Múltiplos campos: ?sort=categoria,asc&sort=preco,desc
 */
@RestController
@RequestMapping("/api/v1/produtos")
class ProdutoController {

    private static final Set<String> CAMPOS_ORDENACAO_PERMITIDOS =
            Set.of("nome", "preco", "estoque", "categoria");

    private final ListarProdutosUseCase listarProdutos;
    private final ProdutoWebMapper mapper;

    ProdutoController(ListarProdutosUseCase listarProdutos, ProdutoWebMapper mapper) {
        this.listarProdutos = listarProdutos;
        this.mapper = mapper;
    }

    @GetMapping
    ResponseEntity<Page<ProdutoResponse>> listar(
            @Valid @ModelAttribute ProdutoFiltroRequest filtroRequest,
            @PageableDefault(size = 20)
            @SortDefault(sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable) {

        var filtro  = mapper.toFiltro(filtroRequest);
        var pagina  = toPagina(pageable);
        var resultado = listarProdutos.executar(filtro, pagina);

        var pageResponse = new PageImpl<>(
                resultado.conteudo().stream().map(mapper::toResponse).toList(),
                PageRequest.of(resultado.paginaAtual(), pageable.getPageSize()),
                resultado.totalElementos()
        );

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * Converte Pageable (Spring) → Pagina (domínio).
     * Filtra campos de ordenação inválidos antes de cruzar a fronteira.
     */
    private Pagina toPagina(Pageable pageable) {
        var ordenacoes = pageable.getSort().stream()
                .filter(order -> CAMPOS_ORDENACAO_PERMITIDOS.contains(order.getProperty()))
                .map(order -> new Ordenacao(
                        order.getProperty(),
                        order.isDescending() ? Direcao.DESC : Direcao.ASC
                ))
                .toList();

        return Pagina.de(pageable.getPageNumber(), pageable.getPageSize(), ordenacoes);
    }
}
