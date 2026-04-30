package com.empresa.produto.infrastructure.adapter.in.web;

import com.empresa.produto.domain.model.Pagina;
import com.empresa.produto.domain.model.Pagina.Direcao;
import com.empresa.produto.domain.model.Pagina.Ordenacao;
import com.empresa.produto.domain.port.in.ListarProdutosFinanceirosUseCase;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFinanceiroFiltroRequest;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFinanceiroResponse;
import com.empresa.produto.infrastructure.adapter.in.web.mapper.ProdutoFinanceiroWebMapper;
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

/**
 * Adapter HTTP — listagem de produtos financeiros.
 *
 * Ordenação permitida (definida no use case):
 *   Campos próprios : nome, preco, estoque, categoria
 *   Relações 1:1   : saldo.valor, limite.valor
 *
 * Campos inválidos são ignorados silenciosamente — fallback: nome, ASC.
 *
 * Exemplos:
 *   GET /api/v1/produtos-financeiros/42
 *   GET /api/v1/produtos-financeiros/42?usuarioId=7&ativo=true
 *   GET /api/v1/produtos-financeiros/42?sort=saldo.valor,desc
 *   GET /api/v1/produtos-financeiros/42?sort=limite.valor,asc&sort=nome,asc
 */
@Validated
@RestController
@RequestMapping("/api/v1/produtos-financeiros")
class ProdutoFinanceiroController {

    private final ListarProdutosFinanceirosUseCase listarProdutos;
    private final ProdutoFinanceiroWebMapper mapper;

    ProdutoFinanceiroController(ListarProdutosFinanceirosUseCase listarProdutos,
                                ProdutoFinanceiroWebMapper mapper) {
        this.listarProdutos = listarProdutos;
        this.mapper = mapper;
    }

    @GetMapping("/{produtoId}")
    ResponseEntity<Page<ProdutoFinanceiroResponse>> listar(
            @PathVariable @Positive(message = "produtoId deve ser maior que zero") Long produtoId,
            @RequestParam(required = false) @Positive(message = "usuarioId deve ser maior que zero") Long usuarioId,
            @Valid @ModelAttribute ProdutoFinanceiroFiltroRequest filtroRequest,
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

    private Pagina toPagina(Pageable pageable) {
        var ordenacoes = pageable.getSort().stream()
                .filter(order -> ListarProdutosFinanceirosUseCase.CAMPOS_ORDENACAO_PERMITIDOS
                        .contains(order.getProperty()))
                .map(order -> new Ordenacao(
                        order.getProperty(),
                        order.isDescending() ? Direcao.DESC : Direcao.ASC
                ))
                .toList();

        return Pagina.de(pageable.getPageNumber(), pageable.getPageSize(), ordenacoes);
    }
}
