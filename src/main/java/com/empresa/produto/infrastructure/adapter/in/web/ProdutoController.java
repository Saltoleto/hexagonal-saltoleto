package com.empresa.produto.infrastructure.adapter.in.web;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Pagina.Direcao;
import com.empresa.produto.domain.model.Pagina.Ordenacao;
import com.empresa.produto.domain.port.in.ListarProdutosUseCase;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFiltroRequest;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoResponse;
import com.empresa.produto.infrastructure.adapter.in.web.mapper.ProdutoWebMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adapter de entrada HTTP — traduz requisições REST em chamadas ao use case.
 *
 * Parâmetros de entrada:
 * - {produtoId}  → @PathVariable  — identifica o contexto do produto na URL
 * - usuarioId    → @RequestParam  — filtro opcional por usuário
 * - demais filtros via @ModelAttribute (ProdutoFiltroRequest)
 *
 * Tradução de paginação:
 * - Entrada:  Pageable (Spring) → Pagina (domínio)  via toPagina()
 * - Saída:    ResultadoPaginado (domínio) → Page<ProdutoResponse> (HTTP)
 *
 * A whitelist de campos ordenáveis pertence ao domínio (ListarProdutosUseCase).
 * O controller apenas a referencia — sem duplicar esse conhecimento aqui.
 */
@Validated
@RestController
@RequestMapping("/api/v1/produtos")
class ProdutoController {

    private final ListarProdutosUseCase listarProdutos;
    private final ProdutoWebMapper mapper;

    ProdutoController(ListarProdutosUseCase listarProdutos, ProdutoWebMapper mapper) {
        this.listarProdutos = listarProdutos;
        this.mapper = mapper;
    }

    /**
     * Lista produtos filtrando pelo contexto de produtoId e opcionalmente por usuarioId.
     *
     * Exemplos:
     *   GET /api/v1/produtos/42
     *   GET /api/v1/produtos/42?usuarioId=7
     *   GET /api/v1/produtos/42?usuarioId=7&nome=notebook&ativo=true
     *   GET /api/v1/produtos/42?sort=preco,desc&page=0&size=10
     */
    @GetMapping("/{produtoId}")
    ResponseEntity<Page<ProdutoResponse>> listar(
            @PathVariable @Positive(message = "produtoId deve ser maior que zero") Long produtoId,
            @RequestParam(required = false) @Positive(message = "usuarioId deve ser maior que zero") Long usuarioId,
            @Valid @ModelAttribute ProdutoFiltroRequest filtroRequest,
            @PageableDefault(size = 20)
            @SortDefault(sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable) {

        var filtro    = mapper.toFiltro(produtoId, usuarioId, filtroRequest);
        var pagina    = toPagina(pageable);
        var resultado = listarProdutos.executar(filtro, pagina);

        var pageResponse = new PageImpl<>(
                resultado.conteudo().stream().map(mapper::toResponse).toList(),
                PageRequest.of(resultado.paginaAtual(), pageable.getPageSize()),
                resultado.totalElementos()
        );

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * Traduz Pageable (Spring) → Pagina (domínio).
     *
     * A filtragem de campos usa ListarProdutosUseCase.CAMPOS_ORDENACAO_PERMITIDOS
     * — a regra de quais campos são ordenáveis vive no domínio, não aqui.
     */
    private Pagina toPagina(Pageable pageable) {
        var ordenacoes = pageable.getSort().stream()
                .filter(order -> ListarProdutosUseCase.CAMPOS_ORDENACAO_PERMITIDOS
                        .contains(order.getProperty()))
                .map(order -> new Ordenacao(
                        order.getProperty(),
                        order.isDescending() ? Direcao.DESC : Direcao.ASC
                ))
                .toList();

        return Pagina.de(pageable.getPageNumber(), pageable.getPageSize(), ordenacoes);
    }
}
