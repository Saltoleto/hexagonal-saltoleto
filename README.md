# produto-api

API REST para listagem de produtos com filtros e paginação.  
Construída com Spring Boot 3, Java 21 e arquitetura hexagonal.

---

## Stack

| Tecnologia        | Versão  | Papel                                 |
|-------------------|---------|---------------------------------------|
| Java              | 21      | Linguagem — Virtual Threads           |
| Spring Boot       | 3.2.5   | Framework principal                   |
| Spring Data JPA   | 3.2.x   | Persistência                          |
| MapStruct         | 1.5.5   | Mapeamento entre camadas              |
| MySQL             | 8.x     | Banco de dados                        |
| Flyway            | 10.x    | Migrações de schema                   |
| H2                | —       | Banco em memória para testes          |

---

## Arquitetura Hexagonal

A arquitetura hexagonal (também chamada de Ports & Adapters) organiza o sistema em torno do domínio, que não depende de nenhum framework ou detalhe de infraestrutura. A infraestrutura — banco de dados, HTTP, mensageria — é tratada como detalhe substituível que se conecta ao domínio por meio de interfaces (ports).

```
                        ┌─────────────────────────────────┐
     HTTP Request       │           INFRASTRUCTURE         │
   ─────────────►  [Controller]                           │
                        │      ┌─────────────────────┐    │
                        │      │      APPLICATION     │    │
                        │      │                      │    │
                        │  [Use Case]─────────────►[Port OUT]──► [JPA Adapter] ──► MySQL
                        │      │   (Port IN impl)     │    │
                        │      │                      │    │
                        │      │    ┌──────────┐      │    │
                        │      │    │  DOMAIN  │      │    │
                        │      │    │  model   │      │    │
                        │      │    │  ports   │      │    │
                        │      │    └──────────┘      │    │
                        │      └─────────────────────┘    │
                        └─────────────────────────────────┘
```

**Regra fundamental:** as dependências sempre apontam para dentro. O domínio não conhece nada além de si mesmo. A aplicação conhece o domínio. A infraestrutura conhece tudo — mas é a única camada que pode ser trocada sem tocar no núcleo.

---

## Estrutura de Pacotes

```
src/main/java/com/empresa/produto/
│
├── domain/                          ← Núcleo da aplicação. Zero dependência externa.
│   ├── model/                       ← Entidades, enums e exceções de domínio.
│   │   ├── Produto.java
│   │   ├── CategoriaProduto.java
│   │   └── DomainException.java
│   └── port/                        ← Contratos que definem como o mundo se conecta ao domínio.
│       ├── in/                      ← O que a aplicação oferece (casos de uso).
│       │   └── ListarProdutosUseCase.java
│       └── out/                     ← O que a aplicação exige do mundo externo.
│           └── ProdutoRepositoryPort.java
│
├── application/                     ← Orquestração. Usa o domínio, fala com ports de saída.
│   └── usecase/
│       └── ListarProdutosService.java
│
└── infrastructure/                  ← Detalhes técnicos. Pode ser trocada sem afetar domínio.
    ├── adapter/
    │   ├── in/                      ← Adaptadores de entrada (o que dispara casos de uso).
    │   │   └── web/
    │   │       ├── ProdutoController.java
    │   │       ├── dto/             ← Contratos HTTP: request e response.
    │   │       └── mapper/          ← MapStruct: DTO ↔ domínio.
    │   └── out/                     ← Adaptadores de saída (o que o domínio aciona).
    │       └── persistence/
    │           ├── ProdutoJpaAdapter.java       ← Implementa ProdutoRepositoryPort.
    │           ├── ProdutoJpaRepository.java    ← Interface Spring Data (package-private).
    │           ├── entity/          ← Entidades JPA: espelho do schema SQL.
    │           ├── mapper/          ← MapStruct: Entity ↔ domínio.
    │           └── specification/   ← Filtros dinâmicos com JPA Criteria.
    └── config/
        └── GlobalExceptionHandler.java
```

---

## Responsabilidade de Cada Camada

### `domain/model/`

O coração da aplicação. Contém apenas Java puro — sem Spring, sem JPA, sem Jackson.

- **Entidades** têm comportamento real. Métodos como `temEstoque()` e `estaDisponivel()` expressam regras de negócio, não flags que o chamador interpreta.
- **Construção controlada** via factory methods estáticos (`Produto.reconstituir(...)`). Sem construtores públicos com todos os campos — o domínio decide como um objeto nasce.
- **Sem setters públicos.** O estado só muda por meio de métodos com intenção de negócio explícita.
- **`DomainException`** representa violação de regra de negócio — não um erro técnico.

> Regra: se você precisar importar algo de fora de `domain/` aqui, pare e reveja o design.

---

### `application/usecase/` — nota sobre `Pageable`

O use case utiliza `Pageable` e `Page` do Spring Data. Isso representa um acoplamento técnico consciente e documentado.

**Por que não abstrair:**
- `Pageable` é uma interface estável — não muda entre versões do Spring.
- Criar tipos próprios (`Pagina`, `ResultadoPaginado`) tem custo real de manutenção sem ganho prático se o projeto não vai trocar de framework.
- O acoplamento fica contido na camada `application` — o `domain/model` permanece puro.

**Quando abstrair faz sentido:** bibliotecas reutilizáveis, SDKs, projetos que precisam rodar com múltiplos frameworks, ou equipes com política explícita de zero dependência de framework no application layer.

---

### `domain/port/in/`

Interfaces que definem **o que a aplicação sabe fazer**. São os casos de uso expostos para o mundo externo.

- Cada interface representa uma intenção de negócio: `ListarProdutosUseCase`, `CriarPedidoUseCase`.
- Os `record` de entrada (como `Filtro`) ficam como tipos internos da interface — mantém o contrato coeso sem proliferar classes soltas.
- O controller chama estas interfaces. Nunca a implementação diretamente.

---

### `domain/port/out/`

Interfaces que definem **o que a aplicação precisa do mundo externo**. São as dependências do domínio declaradas como abstração.

- `ProdutoRepositoryPort` diz: "preciso de algo que liste produtos com filtros". Não sabe se é MySQL, MongoDB ou uma lista em memória.
- O domínio define estas interfaces. A infraestrutura as implementa. Isso é inversão de dependência na prática.

---

### `application/usecase/`

Implementações dos casos de uso. Orquestram o domínio e se comunicam com os ports de saída.

- **`@Service` é correto aqui.** Esta camada não é o domínio — é a aplicação. Usar `@Service` para registro no container Spring é pragmático e não viola nada.
- **`@Transactional(readOnly = true)`** em operações de leitura: desativa dirty checking, melhora performance, e sinaliza intenção.
- Sem lógica de negócio aqui. A lógica fica no domínio. O use case orquestra.

---

### `infrastructure/adapter/in/web/`

Adapter HTTP — traduz o protocolo REST em chamadas ao caso de uso.

- **Controller** tem uma única responsabilidade: receber a requisição, mapear para o contrato do use case, chamar, mapear resposta, devolver HTTP.
- **`@ModelAttribute`** para filtros via query string — mapeia todos os parâmetros para um record limpo sem `@RequestParam` individual para cada campo.
- **`@Valid`** sempre em request bodies e filtros — validação acontece aqui, nunca no domínio.
- **DTOs separados** (`ProdutoFiltroRequest`, `ProdutoResponse`) — o contrato HTTP pode evoluir sem tocar no domínio.

---

### `infrastructure/adapter/in/web/mapper/`

MapStruct — converte entre DTOs da camada web e objetos de domínio.

- **`ProdutoWebMapper`** cruza a fronteira web → domínio (Filtro) e domínio → web (ProdutoResponse).
- Campos computados do domínio (`temEstoque`, `estaDisponivel`) são mapeados via `expression`, delegando para os métodos do próprio domínio — sem duplicar lógica no mapper.
- Gerado em tempo de compilação. Zero reflection em runtime. Zero surpresas silenciosas como ModelMapper.

---

### `infrastructure/adapter/out/persistence/`

Adapter de persistência — implementa o port de saída usando JPA.

- **`ProdutoJpaAdapter`** implementa `ProdutoRepositoryPort`. É a única classe pública neste pacote. Todo o resto é package-private.
- **`ProdutoJpaRepository`** é package-private — nenhum código externo a este pacote pode depender desta interface Spring Data diretamente.
- **`ProdutoEntity`** nunca sai deste pacote. O domínio nunca vê uma `ProdutoEntity`.
- **`ProdutoSpecification`** centraliza os filtros dinâmicos. Cada filtro é um método privado nomeado que retorna `null` quando inativo — o Spring Data ignora specs nulas em `Specification.where().and()`, sem gerar predicados desnecessários. `comFiltro()` vira uma declaração de intenção, não um bloco de ifs.

---

### `infrastructure/adapter/out/persistence/mapper/`

MapStruct — converte entre entidade JPA e modelo de domínio.

- **`ProdutoPersistenceMapper`** cruza a fronteira persistence → domain.
- `categoria` é armazenada como `String` no banco e convertida para enum no domínio.
- Separado do mapper web propositalmente: cada fronteira tem seu próprio mapper.

---

### `infrastructure/config/`

Configurações Spring que não pertencem a nenhum adapter específico.

- **`GlobalExceptionHandler`** centraliza o tratamento de exceções. Garante que nenhum detalhe interno vaze para o cliente e que todos os erros tenham formato consistente.

---

## Por que dois Mappers separados?

```
HTTP Request ──► ProdutoFiltroRequest ──[ProdutoWebMapper]──► Filtro (domínio)
                                                                     │
                                                              ListarProdutosUseCase
                                                                     │
                                                                   Produto (domínio)
                                                                     │
                                                       [ProdutoWebMapper]──► ProdutoResponse ──► HTTP Response

Banco ──► ProdutoEntity ──[ProdutoPersistenceMapper]──► Produto (domínio)
```

- **`ProdutoWebMapper`**: fronteira web ↔ domínio. Conhece DTOs HTTP.
- **`ProdutoPersistenceMapper`**: fronteira banco ↔ domínio. Conhece entidades JPA.

Se o schema do banco mudar, só `ProdutoPersistenceMapper` é afetado.  
Se o contrato da API mudar, só `ProdutoWebMapper` é afetado.  
O domínio não muda em nenhum dos dois casos.

---

## Por que `@Service` no Use Case?

Pergunta legítima na adoção de hexagonal com Spring.

O `@Service` é um estereótipo de componente — diz ao Spring para registrar o bean no container. Ele não adiciona nenhum comportamento de infraestrutura.

A proibição de anotações de framework se aplica ao **domínio** (`domain/model/`, `domain/port/`). O use case vive na camada `application/`, que já tem permissão de conhecer o Spring para fins de orquestração e gerenciamento transacional.

A alternativa — classes `@Configuration` com métodos `@Bean` manuais — adiciona indireção sem nenhum ganho real de desacoplamento. O Spring ainda está presente, só mudou de arquivo.

**Conclusão:** `@Service` no use case é pragmático, amplamente aceito e não viola os princípios da arquitetura hexagonal.

---

## Endpoints

### `GET /api/v1/produtos`

Lista produtos com filtros opcionais e paginação.

| Parâmetro   | Tipo       | Descrição                              |
|-------------|------------|----------------------------------------|
| `nome`      | `string`   | Busca parcial, case-insensitive        |
| `categoria` | `string`   | Valor exato: ELETRONICO, ALIMENTO...   |
| `precoMin`  | `decimal`  | Preço mínimo (inclusive)               |
| `precoMax`  | `decimal`  | Preço máximo (inclusive)               |
| `ativo`     | `boolean`  | Filtra por status ativo/inativo        |
| `page`      | `int`      | Número da página (base 0, padrão: 0)   |
| `size`      | `int`      | Itens por página (padrão: 20)          |
| `sort`      | `string`   | Campo e direção: `preco,desc`. Múltiplos: `&sort=categoria,asc&sort=preco,desc`. Permitidos: `nome`, `preco`, `estoque`, `categoria`. Campos inválidos: ignorados com fallback `nome,asc`. |

**Exemplos:**

```bash
# Listar todos
GET /api/v1/produtos

# Filtrar por nome
GET /api/v1/produtos?nome=notebook

# Eletrônicos ativos entre R$500 e R$3000
GET /api/v1/produtos?categoria=ELETRONICO&precoMin=500&precoMax=3000&ativo=true

# Segunda página, 10 por página, ordenado por preço decrescente
GET /api/v1/produtos?page=1&size=10&sort=preco,desc

# Múltiplos campos de ordenação
GET /api/v1/produtos?sort=categoria,asc&sort=preco,desc

# Campo inválido — ignorado, fallback nome,asc (sem 500)
GET /api/v1/produtos?sort=campoInexistente,desc
```

**Resposta `200 OK`:**

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Smartphone Galaxy A54",
      "descricao": "Tela 6.4\", 128GB, câmera tripla 50MP",
      "preco": 1899.90,
      "estoque": 50,
      "categoria": "ELETRONICO",
      "ativo": true,
      "temEstoque": true,
      "estaDisponivel": true
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true }
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

## Configuração

### Variáveis de ambiente

```bash
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

### `application.yml` (principais decisões)

| Propriedade                       | Valor        | Motivo                                              |
|-----------------------------------|--------------|-----------------------------------------------------|
| `jpa.hibernate.ddl-auto`         | `validate`   | Flyway gerencia schema. JPA apenas valida.          |
| `jpa.open-in-view`               | `false`      | Evita lazy loading fora da transação.               |
| `jpa.show-sql`                   | `false`      | Não polui logs em produção.                         |
| `spring.threads.virtual.enabled` | `true`       | Virtual Threads do Java 21 — throughput sem WebFlux.|

---

## Banco de Dados

Migrações gerenciadas pelo Flyway em `src/main/resources/db/migration/`.

| Migration            | Descrição                         |
|----------------------|-----------------------------------|
| `V1__create_produtos.sql` | Criação da tabela e índices  |
| `V2__seed_produtos.sql`   | Dados iniciais para dev      |

---

## Testes

```bash
mvn test
```

| Tipo                  | Localização                                | O que testa                              |
|-----------------------|---------------------------------------------|------------------------------------------|
| Unitário de domínio   | `domain/ProdutoTest`                        | Regras de negócio puras. Zero Spring.    |
| Slice web             | `infrastructure/.../ProdutoControllerTest`  | Camada HTTP, validação, mapeamento.      |
| Slice persistência    | `infrastructure/.../ProdutoJpaRepositoryTest` | Queries e Specifications contra H2.   |

**Pirâmide de testes:**

```
         /\
        /E2E\          ← poucos, lentos, caros
       /──────\
      / Slice  \       ← médios: @WebMvcTest, @DataJpaTest
     /──────────\
    /  Unitário  \     ← maioria: domínio puro, rápidos, sem Spring
   ──────────────────
```

> Se um teste de domínio precisa de `@SpringBootTest`, a arquitetura está errada.

---

## Executando o projeto

### Pré-requisitos

- Java 21+
- Maven 3.9+
- MySQL 8+ rodando em `localhost:3306`
- Database `produto_db` criada

```sql
CREATE DATABASE produto_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Rodar

```bash
export DB_USERNAME=root
export DB_PASSWORD=sua_senha

mvn spring-boot:run
```

O Flyway executará as migrations automaticamente na primeira inicialização.

---

## Decisões de design resumidas

| Decisão                              | Motivo                                                        |
|--------------------------------------|---------------------------------------------------------------|
| Dois mappers separados               | Cada fronteira evolui de forma independente                   |
| Predicados como métodos privados     | Cada filtro tem nome, é legível e reutilizável. comFiltro() é declaração de intenção |
| `null` em spec inativa               | Spring Data ignora — sem predicado desnecessário na query     |
| Whitelist de campos de ordenação     | Campo inválido vira 500 no JPA — fallback silencioso protege  |
| `@Transactional(readOnly = true)`    | Desativa dirty checking, melhora performance                  |
| `open-in-view: false`                | Transação não pode vazar para a camada de serialização        |
| `ddl-auto: validate`                 | Schema é responsabilidade do Flyway, não do JPA               |
| `@Service` no use case               | Pragmático e correto — use case não é domínio                 |
| `Pageable` do Spring no use case      | Trade-off consciente: `Pageable` é interface estável, custo de abstrair supera o benefício em projetos Spring puros |
| `ProdutoEntity` package-private      | Infraestrutura não pode vazar para fora do adapter            |
| `ProdutoJpaRepository` package-private | Ninguém acessa o repositório JPA diretamente                |
| Virtual Threads habilitado           | Throughput de I/O sem complexidade de WebFlux reativo         |
| Factory method em `Produto`          | Construção controlada — o domínio decide como um objeto nasce |

---


---

## Padrões Reutilizáveis — Guia de Referência

Esta seção documenta os padrões arquiteturais implementados neste projeto de forma independente do domínio. O objetivo é servir como referência para outros projetos hexagonais que precisem implementar os mesmos mecanismos. Nenhuma classe específica deste projeto é referenciada aqui.

---

### Padrão 1 — Conversão nas Bordas de Entrada (HTTP → Domínio)

**Problema:** o controller recebe dados de três mecanismos HTTP distintos (`@PathVariable`, `@RequestParam`, `@ModelAttribute`) e precisa entregá-los ao use case como um único objeto de domínio, sem que o domínio saiba que HTTP existe.

**Solução:** o mapper de entrada (MapStruct) recebe cada fonte separadamente e constrói o objeto de domínio. O controller não monta o objeto — delega ao mapper.

```java
// Controller — coleta as três fontes e delega
var filtro = mapper.toFiltro(pathParam, requestParam, modelAttributeRequest);

// Mapper MapStruct — monta o objeto de domínio
@Mapping(target = "entidadeId",  source = "entidadeId")       // vem do @PathVariable
@Mapping(target = "usuarioId",   source = "usuarioId")        // vem do @RequestParam
@Mapping(target = "nome",        source = "request.nome")     // vem do @ModelAttribute
@Mapping(target = "status",      source = "request.status")
MeuFiltro toFiltro(Long entidadeId, Long usuarioId, MeuFiltroRequest request);
```

**Regras:**
- O DTO de filtro (`MeuFiltroRequest`) só carrega campos que chegam via query string — nunca path params ou request params isolados.
- Path params e request params são passados diretamente como argumentos do método do mapper.
- O objeto de domínio resultante (`MeuFiltro`) vive em `domain/model/` — sem anotações de framework.

---

### Padrão 2 — Conversão de Paginação e Ordenação (Pageable → Pagina)

**Problema:** o Spring resolve `Pageable` automaticamente, mas o domínio não pode depender de `org.springframework.data.domain.Pageable`. A ordenação precisa ser validada antes de cruzar a fronteira — campo inválido causa erro 500 no JPA.

**Solução:** o controller converte `Pageable` para o tipo de domínio `Pagina`, aplicando whitelist de campos permitidos. A whitelist vive no use case — não no controller.

```java
// Use case — fonte de verdade dos campos ordenáveis (regra de domínio)
Set<String> CAMPOS_ORDENACAO_PERMITIDOS = Set.of("nome", "valor", "data", "relacao.campo");

// Controller — converte e filtra
private Pagina toPagina(Pageable pageable) {
    var ordenacoes = pageable.getSort().stream()
            .filter(order -> MeuUseCase.CAMPOS_ORDENACAO_PERMITIDOS
                    .contains(order.getProperty()))
            .map(order -> new Ordenacao(
                    order.getProperty(),
                    order.isDescending() ? Direcao.DESC : Direcao.ASC
            ))
            .toList();

    return Pagina.de(pageable.getPageNumber(), pageable.getPageSize(), ordenacoes);
}
```

**Regras:**
- `CAMPOS_ORDENACAO_PERMITIDOS` é uma constante de interface — implicitamente `public static final`.
- Campos inválidos são descartados silenciosamente — fallback para o `@SortDefault` declarado no método.
- Para ordenar por campos de relações, incluir o path completo na whitelist: `"relacao.campo"`.
- O tipo `Pagina` e seus records internos (`Ordenacao`, `Direcao`) são independentes de framework.

**Configuração no controller:**
```java
@GetMapping("/{id}")
ResponseEntity<Page<MeuResponse>> listar(
        @PathVariable Long id,
        @PageableDefault(size = 20)
        @SortDefault(sort = "nome", direction = Sort.Direction.ASC)
        Pageable pageable) { ... }
```

---

### Padrão 3 — Filtro como Tipo de Domínio

**Problema:** o filtro de consulta contém termos do vocabulário de negócio. Se ficar como record interno do use case, fica acoplado a um caso de uso específico e não pode ser reutilizado.

**Solução:** o filtro vive em `domain/model/` como record imutável com todos os campos opcionais.

```java
// domain/model/MeuFiltro.java
public record MeuFiltro(
        Long entidadeId,     // pode vir de path param
        Long usuarioId,      // pode vir de request param
        String nome,         // query string
        String status,       // query string
        BigDecimal valorMin,
        BigDecimal valorMax,
        Boolean ativo
) {}
```

**Regras:**
- Todos os campos são opcionais — `null` significa "sem restrição".
- Zero anotações de framework.
- Nomeado pelo conceito de domínio, não pelo use case (`ContaFiltro`, não `ListarContasRequest`).
- Reutilizável por qualquer use case futuro (relatório, exportação, dashboard).

---

### Padrão 4 — Specification com Múltiplos Filtros Opcionais

**Problema:** filtros opcionais combinados geram explosão de métodos no repository ou um método com muitos `if`s acumulando predicados numa lista — difícil de ler, testar e estender.

**Solução:** cada filtro é um método privado nomeado que retorna `null` quando inativo. O Spring Data ignora specs nulas em `Specification.where().and()` — sem predicado desnecessário na query.

```java
public final class MinhaSpecification {

    private MinhaSpecification() {}

    public static Specification<MinhaEntity> comFiltro(MeuFiltro filtro) {
        return Specification
                .where(comEntidadeId(filtro.entidadeId()))
                .and(comUsuarioId(filtro.usuarioId()))
                .and(comNome(filtro.nome()))
                .and(comStatus(filtro.status()))
                .and(comValorMinimo(filtro.valorMin()))
                .and(comValorMaximo(filtro.valorMax()))
                .and(comAtivo(filtro.ativo()));
        // Adicionar novo filtro = novo método privado + uma linha aqui
    }

    private static Specification<MinhaEntity> comNome(String nome) {
        return (root, query, cb) ->
                (nome == null || nome.isBlank()) ? null
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    private static Specification<MinhaEntity> comValorMinimo(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? null
                : cb.greaterThanOrEqualTo(root.get("valor"), min);
    }

    private static Specification<MinhaEntity> comAtivo(Boolean ativo) {
        return (root, query, cb) ->
                ativo == null ? null
                : cb.equal(root.get("ativo"), ativo);
    }
}
```

**Regras:**
- Retornar `null` (não `cb.conjunction()`) quando o filtro for inativo — evita `1=1` desnecessário.
- Um método privado por filtro — nome descritivo, testável isoladamente.
- `comFiltro()` é uma declaração de intenção, não um bloco de código.
- Busca parcial case-insensitive: `cb.lower()` + `%valor%`.
- Busca exata em enums/categorias: `.toUpperCase()` antes de comparar.
- **Não utilizar JPA Metamodel Estático** (`MinhaEntity_.campo`). Os campos são referenciados por `String` (`root.get("campo")`). O metamodel adiciona complexidade de configuração e geração de código sem ganho proporcional em projetos com Specification bem estruturada — erros de nome de campo são detectados em tempo de teste, não de compilação, o que é suficiente dado o padrão de testes adotado.

---

### Padrão 5 — Specification com JOIN para Relações

**Problema:** quando o endpoint sempre retorna dados de relações (`@OneToOne`, `@OneToMany`), o carregamento lazy gera N+1 queries. Quando a ordenação é por campo de uma relação, o JPA precisa do JOIN para resolver o `ORDER BY`.

**Solução em dois níveis:**

**Nível 1 — `@EntityGraph` no repository** (para relações sempre carregadas):
```java
// Repository — LEFT JOIN FETCH sempre, sem N+1
@EntityGraph(attributePaths = {"relacaoA", "relacaoB"})
Page<MinhaEntity> findAll(Specification<MinhaEntity> spec, Pageable pageable);
```
O `@EntityGraph` garante que as relações sempre vêm na mesma query. Como o JOIN já existe, ordenar por `relacaoA.campo` funciona sem nenhuma lógica adicional no adapter.

**Nível 2 — JOIN condicional na Specification** (para relações opcionais, não sempre carregadas):
```java
// Specification — JOIN apenas quando necessário para ordenação
public static Specification<MinhaEntity> comJoinRelacaoA() {
    return (root, query, cb) -> {
        root.join("relacaoA", JoinType.LEFT);
        return null; // só adiciona o join, sem predicado
    };
}

// Adapter — aplica o join se o campo de ordenação exigir
if (pagina.ordenacoes().stream().anyMatch(o -> o.campo().startsWith("relacaoA."))) {
    spec = spec.and(MinhaSpecification.comJoinRelacaoA());
}
```

**Quando usar cada um:**

| Cenário | Solução |
|---|---|
| Relação sempre retornada na response | `@EntityGraph` no repository |
| Relação opcional, ordenação possível | JOIN condicional na Specification |
| Relação opcional, sem ordenação | `fetch = LAZY` — carregamento sob demanda |

**Regras:**
- `@EntityGraph` elimina N+1 sem alterar o mapeamento da entity.
- JOIN via Specification retorna `null` como predicado — só adiciona o join à query.
- `JoinType.LEFT` preserva registros sem a relação no resultado.
- A whitelist de campos ordenáveis deve incluir o path completo: `"relacaoA.campo"`.

---

### Padrão 6 — Conversão de Saída (Domínio → HTTP)

**Problema:** o domínio retorna `ResultadoPaginado<T>` — tipo próprio. O cliente HTTP espera `Page<T>` do Spring serializado como JSON paginado. Os objetos de domínio precisam ser convertidos para DTOs de response.

**Solução:** o controller reconstrói o `Page` do Spring a partir do `ResultadoPaginado`, e o mapper web converte cada item.

```java
// Controller — reconstrói Page<Response> a partir do ResultadoPaginado
var pageResponse = new PageImpl<>(
        resultado.conteudo().stream().map(mapper::toResponse).toList(),
        PageRequest.of(resultado.paginaAtual(), pageable.getPageSize()),
        resultado.totalElementos()
);
return ResponseEntity.ok(pageResponse);

// Mapper web (MapStruct) — converte domínio → DTO
@Mapping(target = "status",    expression = "java(entidade.getStatus().name())")
@Mapping(target = "temValor",  expression = "java(entidade.temValor())")
@Mapping(target = "relacaoA",  expression = "java(toRelacaoAResponse(entidade.getRelacaoA()))")
MeuResponse toResponse(MinhaEntidade entidade);

default RelacaoAResponse toRelacaoAResponse(MinhaRelacao relacao) {
    return relacao == null ? null : new RelacaoAResponse(relacao.id(), relacao.valor());
}
```

**Regras:**
- Campos computados do domínio são mapeados via `expression` — sem duplicar lógica no mapper.
- Relações opcionais retornam `null` no response — nunca lançar exceção por ausência.
- Enums são convertidos para `String` via `.name()`.
- Cada camada tem seu próprio mapper — o mapper web nunca conhece entities JPA.

---

### Tipos de Domínio Reutilizáveis

Os seguintes tipos são independentes de domínio e podem ser copiados diretamente para qualquer projeto hexagonal:

**`Pagina.java`** — substitui `Pageable` do Spring nas camadas domain e application.
**`ResultadoPaginado.java`** — substitui `Page<T>` do Spring. Possui método `map()` para transformação.
**`DomainException.java`** — exceção base para violações de regra de negócio.

```java
// Pagina — copiar sem alteração
public record Pagina(int numero, int tamanho, List<Ordenacao> ordenacoes) {
    public record Ordenacao(String campo, Direcao direcao) {}
    public enum Direcao { ASC, DESC }

    public static Pagina de(int numero, int tamanho, List<Ordenacao> ordenacoes) {
        if (numero < 0) throw new DomainException("Número da página não pode ser negativo");
        if (tamanho < 1) throw new DomainException("Tamanho da página deve ser maior que zero");
        return new Pagina(numero, tamanho, ordenacoes == null ? List.of() : ordenacoes);
    }
}

// ResultadoPaginado — copiar sem alteração
public record ResultadoPaginado<T>(
        List<T> conteudo, long totalElementos, int totalPaginas,
        int paginaAtual, boolean primeira, boolean ultima) {

    public <R> ResultadoPaginado<R> map(Function<T, R> mapper) {
        return new ResultadoPaginado<>(conteudo.stream().map(mapper).toList(),
                totalElementos, totalPaginas, paginaAtual, primeira, ultima);
    }
}
```

---

## Prompt para AI Agent

Use o prompt abaixo ao solicitar que um agente de IA implemente uma feature de listagem em um projeto hexagonal seguindo todos os padrões acima.

---

```
Implemente uma feature de listagem para [NOME DA ENTIDADE] seguindo rigorosamente os padrões de arquitetura hexagonal abaixo. Não desvie deles.

## Estrutura de pacotes obrigatória

domain/model/
  [Entidade].java                      ← POJO puro, zero anotações de framework
  [Entidade]Filtro.java                ← record imutável, todos os campos opcionais (null = sem restrição)
  Pagina.java                          ← tipo próprio de paginação (copiar do projeto de referência)
  ResultadoPaginado.java               ← tipo próprio de resultado paginado (copiar do projeto de referência)
  DomainException.java                 ← exceção base de domínio

domain/port/in/
  Listar[Entidade]sUseCase.java        ← interface com Set<String> CAMPOS_ORDENACAO_PERMITIDOS

domain/port/out/
  [Entidade]RepositoryPort.java        ← interface com método listar([Entidade]Filtro, Pagina)

application/usecase/
  Listar[Entidade]sService.java        ← @Service @Transactional(readOnly=true), implementa o use case

infrastructure/adapter/in/web/
  [Entidade]Controller.java            ← @RestController, converte Pageable→Pagina e ResultadoPaginado→Page
  dto/[Entidade]FiltroRequest.java     ← somente query params (@ModelAttribute), sem path/request params
  dto/[Entidade]Response.java          ← record com campos computados e relações aninhadas quando houver
  mapper/[Entidade]WebMapper.java      ← MapStruct: toFiltro(pathParam, requestParam, request) e toResponse()

infrastructure/adapter/out/persistence/
  [Entidade]JpaAdapter.java            ← implementa RepositoryPort, converte Pagina→Pageable
  [Entidade]JpaRepository.java         ← package-private, @EntityGraph se relações sempre carregadas
  entity/[Entidade]Entity.java         ← package-private, @Entity
  mapper/[Entidade]PersistenceMapper.java ← MapStruct: toDomain()
  specification/[Entidade]Specification.java ← Specification com métodos privados por filtro

## Regras obrigatórias — sem exceção

### Domínio
- Zero anotações Spring/JPA/Jackson em domain/model/ e domain/port/
- Entidades com factory method estático (reconstituir(...)) e sem setters públicos
- Comportamento de negócio em métodos da entidade (temValor(), estaAtivo()), nunca no mapper ou serviço
- [Entidade]Filtro em domain/model/ — nunca como record interno do use case

### Use Case
- CAMPOS_ORDENACAO_PERMITIDOS é uma constante de interface (public static final implícito)
- Incluir campos de relações no formato "relacao.campo" quando aplicável
- @Service e @Transactional(readOnly=true) são as únicas anotações Spring permitidas aqui

### Controller
- @Validated na classe para ativar validação de @PathVariable e @RequestParam
- @PageableDefault(size=20) + @SortDefault(sort="campo", direction=ASC) no Pageable
- Método privado toPagina(Pageable) faz a conversão e aplica o filtro da whitelist:
    .filter(order -> MeuUseCase.CAMPOS_ORDENACAO_PERMITIDOS.contains(order.getProperty()))
- A whitelist é referenciada do use case — nunca declarada no controller
- Reconstrução do Page na saída: new PageImpl<>(lista, PageRequest.of(...), total)

### Mapper Web (MapStruct)
- toFiltro recebe path params e request params como argumentos separados do record de filtro request
- toResponse mapeia campos computados via expression="java(entidade.metodo())"
- Relações opcionais: método default que retorna null quando a relação for null

### Specification
- Um método privado por filtro, retornando null quando o campo for null/blank
- Nunca usar lista de Predicate com ifs — usar Specification.where().and()
- Retornar null (não cb.conjunction()) em specs inativas
- Busca parcial: cb.like(cb.lower(root.get("campo")), "%" + valor.toLowerCase() + "%")
- Referenciar campos por String — root.get("campo"). Não utilizar JPA Metamodel Estático
  (classes geradas com sufixo _ como MinhaEntity_.campo). O padrão adotado usa String literals
  e detecta erros de campo em tempo de teste, evitando a complexidade de configuração do metamodel.

### Repository
- Interface package-private — nunca pública
- @EntityGraph(attributePaths={"relacaoA","relacaoB"}) quando relações são sempre retornadas na response
- O @EntityGraph resolve N+1 e viabiliza ORDER BY por campos das relações sem lógica adicional no adapter

### Adapter JPA
- Único lugar onde Pageable, Page, Sort e qualquer tipo do Spring Data existem
- Converte Pagina→Pageable via método privado toPageable()
- Constrói ResultadoPaginado a partir dos campos do Page retornado pelo repository
- Se @EntityGraph está no repository, não há lógica de JOIN no adapter

### Tipos de domínio para paginação
Copiar exatamente do projeto de referência sem alteração:
- Pagina com records internos Ordenacao e enum Direcao, com validação no factory method
- ResultadoPaginado com método map(Function<T,R>)

### Banco de dados
- Migrations Flyway (V{n}__descricao.sql)
- Índices nas colunas usadas em filtros e ordenações, incluindo FKs de relações
- ddl-auto: validate — Flyway gerencia schema, JPA apenas valida

## O que NÃO fazer
- Não colocar @Entity, @Autowired ou qualquer anotação Spring/JPA em domain/
- Não usar Pageable ou Page em domain/ ou application/
- Não declarar CAMPOS_ORDENACAO_PERMITIDOS no controller
- Não incluir path params ou request params isolados dentro do FiltroRequest
- Não usar ModelMapper — somente MapStruct com geração em compile time
- Não usar JPA Metamodel Estático (classes com sufixo _)
- Não usar open-in-view: true
- Não usar ddl-auto: create ou update em produção
- Não expor Entity JPA fora do pacote de persistência
- Não chamar JpaRepository diretamente fora do adapter de persistência
```
