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
| `ProdutoEntity` package-private      | Infraestrutura não pode vazar para fora do adapter            |
| `ProdutoJpaRepository` package-private | Ninguém acessa o repositório JPA diretamente                |
| Virtual Threads habilitado           | Throughput de I/O sem complexidade de WebFlux reativo         |
| Factory method em `Produto`          | Construção controlada — o domínio decide como um objeto nasce |
