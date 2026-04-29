package com.empresa.produto.infrastructure.adapter.in.web;

import com.empresa.produto.domain.port.in.ListarProdutosUseCase;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoFiltroRequest;
import com.empresa.produto.infrastructure.adapter.in.web.dto.ProdutoResponse;
import com.empresa.produto.infrastructure.adapter.in.web.mapper.ProdutoWebMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

import java.util.Set;

/**
 * Adapter de entrada HTTP — traduz requisições REST em chamadas ao use case.
 *
 * Responsabilidades exclusivas:
 * - Receber e validar a requisição HTTP.
 * - Mapear DTO de entrada para o contrato do use case.
 * - Chamar o use case.
 * - Mapear o resultado para DTO de saída e retornar a resposta HTTP.
 *
 * Ordenação:
 * - Controlada pelo cliente via query param: ?sort=preco,desc
 * - Múltiplos campos: ?sort=categoria,asc&sort=preco,desc
 * - Campos inválidos são ignorados silenciosamente — fallback: nome,asc
 * - Campos permitidos: nome, preco, estoque, categoria
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

        var filtro = mapper.toFiltro(filtroRequest);
        var resultado = listarProdutos.executar(filtro, sanitizarOrdenacao(pageable));
        return ResponseEntity.ok(resultado.map(mapper::toResponse));
    }

    /**
     * Filtra a ordenação recebida mantendo apenas campos da whitelist.
     * Evita que o cliente provoque erro 500 passando campos inexistentes.
     * Se nenhum campo válido restar, aplica o fallback: nome, ASC.
     */
    private Pageable sanitizarOrdenacao(Pageable pageable) {
        var ordersValidos = pageable.getSort().stream()
                .filter(order -> CAMPOS_ORDENACAO_PERMITIDOS.contains(order.getProperty()))
                .toList();

        var sort = ordersValidos.isEmpty()
                ? Sort.by(Sort.Direction.ASC, "nome")
                : Sort.by(ordersValidos);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
