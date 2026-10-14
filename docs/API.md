# API REST

Base: `/api/v1`. Documentação OpenAPI em `/v3/api-docs` e Swagger UI em `/swagger-ui/index.html`, acessíveis após autenticação ADMIN. Os DTOs são o contrato completo e aparecem no OpenAPI gerado.

## Público

| Método | Rota | Conteúdo |
|---|---|---|
| GET | `/public/profile`, `/public/settings` | Configuração pública permitida |
| GET | `/public/professional-profiles` | Perfis ativos, vínculos públicos |
| GET | `/public/projects`, `/public/projects/{slug}` | Projetos publicados |
| GET | `/public/certificates`, `/public/certificates/{slug}` | Certificados publicados |
| GET | `/public/experiences`, `/public/skills`, `/public/resumes` | Coleções publicadas |
| GET | `/public/resumes/{id}/download` | PDF de currículo publicado |
| GET | `/public/files/{id}/download?inline=false` | Arquivo com vínculo público autorizado |
| GET | `/public/github/repositories` | Seleções manuais e cache GitHub |
| POST | `/public/analytics/events` | Evento agregado, se habilitado; exige CSRF |

Coleções: `page` (0+), `size` (1–100), `sort` (`relevance`, `recent`, `oldest`), `search`. Filtros específicos: `category`, `status`, `skill` UUID, `professionalProfile` UUID para projetos, `issuer` textual para certificados, `locale` para currículos. Conteúdo traduzido retorna o mapa completo; o front-end seleciona o idioma e sinaliza fallback PT-BR. Parâmetros sem aplicação naquele domínio não afetam a consulta.

```json
{"content":[],"page":0,"size":20,"totalElements":0,"totalPages":0}
```

## Sessão

`GET /auth/csrf` retorna `{ "token": "…", "headerName": "X-CSRF-TOKEN" }`. Preserve cookies e envie o token nas mutações, inclusive `POST /auth/login`:

```json
{"username":"seu-usuario","password":"sua-senha"}
```

Após login, obtenha CSRF novamente. `GET /auth/me` retorna username; `POST /auth/logout` invalida a sessão. Expiração: 30 minutos. Login possui limite de dez tentativas por endereço em quinze minutos. Erro de credenciais é genérico.

## Administrativo

`/admin/projects`, `/admin/certificates`, `/admin/experiences`, `/admin/skills`, `/admin/resumes`, `/admin/courses`, `/admin/applications`, `/admin/profiles`: GET lista, POST cria, GET `/{id}`, PUT `/{id}`, DELETE `/{id}`. Sem PATCH parcial genérico: PUT recebe o DTO completo.

`PATCH /admin/{projects|certificates|experiences|skills|resumes}/{id}/publication` recebe `{ "published": true }`. PDF é obrigatório para publicar certificado/currículo; currículo principal precisa estar publicado.

`GET/POST /admin/applications/{id}/events`: lista histórico ou altera etapa com `{ "status": "interview", "notes": "Observação privada opcional" }`. PUT da candidatura também registra mudança de status, transacionalmente.

`POST /admin/files` recebe multipart no campo `file`; GET `/{id}` retorna metadados permitidos; GET `/{id}/download` baixa o arquivo privado autenticado; DELETE `/{id}` rejeita vínculos existentes e enfileira exclusão física segura. Nunca retorna storage_key ou credenciais.

`GET /admin/dashboard`, `GET /admin/analytics`, `GET/PUT /admin/settings` retornam indicadores e configuração real. Dados de cursos e candidaturas não possuem endpoints públicos.

Exemplo de criação de projeto (campos demonstrativos claramente identificados):

```json
{
 "slug":"projeto-de-teste",
 "category":"backend",
 "status":"in-progress",
 "featured":false,
 "published":false,
 "displayOrder":0,
 "skillIds":[],
 "mediaIds":[],
 "translations":{"pt-BR":{"title":"Projeto de teste","description":"Exemplo de contrato, não conteúdo profissional"}}
}
```

IDs de vínculo usam UUIDs existentes. Para associar upload, use `id` retornado como `fileId`. Para imagens de projeto, use `mediaIds`. Perfis usam `skillIds`, `projectIds` e `defaultResumeId`.

## Respostas e erros

POST cria com 201; DELETE conteúdo/arquivo retorna 204; mutações de sessão/etapa podem responder corpo vazio 200. Downloads locais retornam bytes e Content-Disposition; S3 retorna 302 para URL assinada curta, nunca um bucket público. Conteúdo oculto e arquivo sem vínculo público retornam 404.

```json
{"code":"VALIDATION","message":"Revise os campos","fields":{"slug":"must not be blank"}}
```

400: entrada inválida; 401: sessão ausente/credenciais inválidas; 403: autorização ou CSRF; 404: recurso inexistente/oculto; 409: slug duplicado/vínculo impeditivo; 413: tamanho excedido; 429: limite de login; 503: armazenamento local indisponível. Erros inesperados não expõem stack trace. Datas ISO 8601; campos opcionais podem ser null.
