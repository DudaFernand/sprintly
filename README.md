# Sprintly

API backend de board Kanban/sprint. Organização multi-tenant, boards com colunas configuráveis, tarefas, comentários com menção, burndown e notificações assíncronas. Sem frontend neste repo.

**Stack:** Java 21 · Spring Boot 4 · PostgreSQL · RabbitMQ · JWT (access + refresh) · Flyway

### Destaques técnicos

- RBAC por membership na organização (leituras e escritas passam por `AuthorizationService`)
- Refresh token com rotação + sliding expiration
- Burndown reconstruído a partir do audit log (trade-off documentado abaixo)
- Notificações assíncronas via RabbitMQ
- Schema versionado com Flyway (`ddl-auto=validate`)
- OpenAPI/Swagger + CI (testes + build Docker)

## O que tem

- Multi-tenant por organização (`ADMIN` / `MEMBER`)
- Hierarquia `Organization → Team → Project → Board → Status/Task`
- Status por board, com flag `done` (é o que define “concluído”)
- Sprints + burndown diário
- Comentários com `@email` → menção + notificação
- Audit log de mudança de status
- Auth com access curto e refresh rotacionado

## Arquitetura

```
HTTP (Bearer JWT)
  → Controllers → Services → Repositories (JPA)
                    ├── AuthorizationService  (membership na org)
                    ├── AuditService
                    └── NotificationPublisher
                              │
                              ▼
                         RabbitMQ  →  NotificationConsumer  →  DB
```

Localmente o Compose sobe Postgres (`5433`), RabbitMQ (`5672`, UI em `15672`) e, se quiser, a app (`8081`).

### Modelo de dados

```mermaid
erDiagram
    User ||--o{ Membership : has
    Organization ||--o{ Membership : has
    Organization ||--o{ Team : contains
    Team ||--o{ Project : contains
    Project ||--o{ Board : contains
    Project ||--o{ Sprint : contains
    Project ||--o{ Epic : contains
    Board ||--o{ Status : columns
    Board ||--o{ Task : has
    Status ||--o{ Task : current
    Sprint ||--o{ Task : plans
    User ||--o{ Task : reports
    Task ||--o{ Comment : has
    Comment ||--o{ CommentMention : mentions
    User ||--o{ Notification : receives
    User ||--o{ AuditLog : generates
    User ||--o{ RefreshToken : owns

    Membership {
        string role
    }
    Status {
        int sortOrder
        boolean done
    }
    Task {
        int storyPoints
        string type
        string priority
    }
    RefreshToken {
        boolean revoked
        datetime expiresAt
    }
```

Autorização não usa roles do Spring Security. Sobe a cadeia até a org (`task → board → project → team → organization`) e consulta `Membership`.

## Decisões técnicas

### RBAC em cadeia

Papel vive na organização, não no board.

- Criar org → criador vira `ADMIN`
- Mutações estruturais (team, project, board, status, sprint, membership) pedem admin
- Dia a dia (task, comentário, listagens) pedem só membership

Evita ACL espalhada em cada nível. Modelo mental: você está na org ou não; se está, ou manda nela ou não.

### Status configurável + `done`

Cada board define as próprias colunas (`name`, `sortOrder`, `done`). Não existe status hardcoded — o que conta como pronto é o que o admin marcar via `PATCH /status/{id}/done`.

Burndown e “tarefa concluída” dependem disso. Sem nenhuma coluna `done`, o burndown não queima ponto nenhum.

### Burndown a partir do audit log

Em vez de uma tabela de histórico de story points, o burndown reconstrói o progresso a partir dos `STATUS_CHANGE` em `audit_logs`.

`GET /sprints/{id}/burndown` anda dia a dia no intervalo da sprint, vê se houve transição para um status `done` até aquele dia, e calcula remaining vs linha ideal.

**Trade-off:** uma fonte de verdade (o log), mas o cálculo fica acoplado ao formato de `changes` e ao `storyPoints` *atual* da task — não ao valor no momento da conclusão. Apagar log ou mudar o texto do audit quebra o gráfico. Para o tamanho atual do projeto, a simplicidade vale; em produção séria materializaria eventos de conclusão.

### Refresh token: rotação + sliding expiration

- Access JWT ~15 min (`jwt.access-expiration`)
- Refresh opaco (64 bytes aleatórios), persistido; TTL via `jwt.refresh-expiration` (default 30 dias)
- Em `POST /auth/refresh`: o antigo é revogado e sai um novo par, com nova data de expiração (sliding)

Ainda não tem logout. Token antigo para de valer na próxima rotação; enquanto estiver válido e não revogado, continua usável.

### Notificações via RabbitMQ

Menção em comentário e mudança de status (task com assignee) publicam em `sprintly.events` (`notification.*`). O consumer grava `Notification` no banco; a API só lê em `GET /notifications`.

Se o Rabbit cair no momento do publish, a request HTTP falha junto — não tem outbox. Aceitável agora; outbox (ou retry) seria o caminho se a entrega precisar ser confiável de verdade.

### Migrações (Flyway)

O schema é versionado em `src/main/resources/db/migration/`. Hibernate fica em `ddl-auto=validate` — não altera tabela sozinho.

Banco já existente (dev local): `baseline-on-migrate=true` registra a V1 sem recriar. Banco fresco (CI): a V1 sobe o schema do zero.

Testes usam H2 com `ddl-auto=create-drop`, Flyway desligado e listeners RabbitMQ com `auto-startup=false`. O CI sobe Postgres + RabbitMQ como services.

## Endpoints

Base: `http://localhost:8081`  
Auth: `Authorization: Bearer <accessToken>` (exceto `/auth/**`)

### Auth

| Método | Path | |
|--------|------|---|
| POST | `/auth/register` | `email`, `password` → tokens |
| POST | `/auth/login` | idem |
| POST | `/auth/refresh` | `refreshToken` → novo par (rotação) |

### Organização

| Método | Path | |
|--------|------|---|
| POST | `/organizations` | cria org + membership ADMIN |
| GET | `/organizations` | só orgs em que o usuário tem membership |
| POST | `/memberships` | admin |
| GET | `/memberships?organizationId=` | |
| POST | `/teams` | admin |
| GET | `/teams?organizationId=` | |
| POST | `/projects` | admin |
| GET | `/projects?teamId=` | |
| POST | `/boards` | |
| GET | `/boards?projectId=` | |

### Board / sprint / task

| Método | Path | |
|--------|------|---|
| POST | `/status` | admin |
| GET | `/status?boardId=` | |
| DELETE | `/status/{id}?boardId=` | admin |
| PATCH | `/status/{id}/done` | `{ "done": true }` |
| POST | `/sprints` | |
| GET | `/sprints?projectId=` | |
| GET | `/sprints/{id}/burndown` | pontos + linha ideal |
| POST | `/tasks` | |
| GET | `/tasks?boardId=` | |
| PATCH | `/tasks/{id}/status` | `{ "statusId": ... }` (+ audit) |
| PATCH | `/tasks/{taskId}/sprint` | `{ "sprintId": ... }` |
| POST/DELETE | `/tasks/{taskId}/labels/{labelId}` | |

### Colaboração

| Método | Path | |
|--------|------|---|
| POST | `/comments` | `@email` gera menção |
| GET | `/comments?taskId=` | |
| GET | `/users?organizationId=` | membros da org (exige membership) |
| GET | `/notifications` | do usuário autenticado |
| GET | `/audit-logs?entityType=&entityId=` | |
| GET | `/audit-logs/{entityType}/{entityId}` | mesma consulta via path |

Bodies seguem as entidades/DTOs — o caminho mais rápido de explorar ainda é o Swagger ou os testes.

## Como rodar

Precisa de Java 21 e Docker (pro Postgres e Rabbit).

```bash
docker compose up -d postgres rabbitmq
```

Postgres em `localhost:5433`. Rabbit em `5672` (management: `http://localhost:15672`, user/pass `sprintly`).

Coloque o segredo JWT no `.env` na raiz (o app carrega esse arquivo no startup; não vai pro git):

```env
JWT_SECRET=troque-por-uma-chave-longa-e-aleatoria
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Várias origins: separe por vírgula (`http://localhost:3000,http://localhost:5173`).

Suba a API:

```bash
./mvnw spring-boot:run
```

Ou o stack inteiro (Compose também lê o `.env`):

```bash
docker compose up --build
```

API em `http://localhost:8081`. Swagger UI em `http://localhost:8081/swagger-ui.html`. Config base em `application.properties`; no CI/Docker, `SPRING_*` e `JWT_*` sobrescrevem o necessário.

## Testes

```bash
./mvnw test
# ou
./mvnw clean verify
```

| Classe | O que cobre |
|--------|-------------|
| `AuthorizationServiceTest` | admin / member / sem membership |
| `BurndownServiceTest` | total SP, done antes da sprint, sprint inexistente |
| `CommentServiceTest` | menção válida, sem menção, email inexistente |
| `TaskRepositoryTest` | `@DataJpaTest` + H2 — `countByStatusId`, `findByBoardId` |
| `SprintlyApplicationTests` | sobe o contexto |

Serviços com Mockito; repository test usa H2 (não precisa do Postgres local). O smoke da aplicação espera Postgres/Rabbit acessíveis com a config padrão — no CI isso vem dos services do workflow.

## CI/CD

`.github/workflows/ci.yml` (push/PR em `main`):

1. Postgres 16 + RabbitMQ como services
2. Java 21 (Temurin) + cache Maven
3. `./mvnw clean verify`
4. `docker build` da imagem

O `Dockerfile` é multi-stage (Maven → JRE Alpine). Não tem deploy automático — o pipeline para em build + testes + imagem.

## Próximos passos

- Logout / revoke e **reuse detection** no refresh (token revogado reaparecendo → invalidar a família)
- Anexos em comentários
- Outbox (ou retry) nas notificações
- Materializar eventos de conclusão pro burndown não depender do texto do audit
- Papéis mais granulares (ex.: por projeto), se o multi-tenant crescer
- DnD no board (front) e issue key estilo `PROJ-123`

## Estrutura

```
src/main/java/com/mariafernandes/sprintly/
  controller/
  service/        # regras, burndown, authz, consumer Rabbit
  domain/
  repository/
  security/       # JWT filter, JwtService, refresh generator
  dto/
  config/         # RabbitMQ, etc.
```
