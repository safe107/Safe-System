# PROMPT MESTRE — SAFE SYSTEM

## PAPEL E OBJETIVO

Atue como um engenheiro de software sênior full stack, arquiteto de sistemas, especialista em UI/UX e segurança de aplicações web.

Sua missão é PROJETAR, IMPLEMENTAR, TESTAR e DOCUMENTAR uma aplicação web completa chamada **SAFE System — Personal Career Operating System**.

Não crie apenas um layout ou protótipo estático. Quero um sistema real, funcional, organizado, seguro e preparado para publicação.

O SAFE System terá duas interfaces integradas:

1. **Portfólio público:** currículo digital, projetos, certificados, experiências, habilidades e contato.
2. **Painel administrativo privado:** gerenciamento do conteúdo público, arquivos, currículos, cursos, estudos, candidaturas e métricas.

A aplicação pertence a um único profissional, com um único administrador. Não implemente cadastro público, perfis de outros usuários ou funcionalidades multi-tenant.

O produto também servirá como projeto de destaque no portfólio do proprietário. Portanto, a qualidade da arquitetura e do código é tão importante quanto o resultado visual.

## 1. CONTEXTO DO PROPRIETÁRIO

Nome profissional: José Miguel.

Formação: estudante de Sistemas de Informação na UNIFESSPA.

Áreas de interesse profissional:
- Suporte técnico e infraestrutura de TI.
- Desenvolvimento back-end, especialmente Java e Spring Boot.
- Redes e segurança da informação.

Experiências e projetos conhecidos, que podem servir como conteúdo inicial:

**JBS Couros Marabá**
- Experiência administrativa com atividades de suporte técnico interno.
- Atendimento a demandas envolvendo usuários, impressoras e ferramentas digitais.
- Desenvolvimento de automações com Excel/VBA e processos de acompanhamento.

**ExceptionJR**
- Experiência de liderança como presidente de empresa júnior.
- Participação em projetos de desenvolvimento de software.
- Gestão de atividades, equipes e projetos.

**Alerta Digital**
- Projeto acadêmico de educação e prevenção de golpes digitais.
- Simulação de cenários relacionados a golpes no WhatsApp.
- Stack: Spring Boot, React, Vite e MySQL.

**ATRPAP**
- Desenvolvimento de landing page e painel administrativo.
- Stack conhecida: React e Vite.

**Diet API**
- Projeto back-end utilizando TypeScript, Fastify, Knex e SQLite.

**LevelTask**
- Projeto pessoal de aplicação CLI com elementos de progressão.

Outros projetos e experiências deverão poder ser cadastrados futuramente.

IMPORTANTE:
Não invente empresas, datas, resultados mensuráveis, certificados, URLs de GitHub, números de projetos ou qualificações. Quando faltar informação, crie estruturas vazias, estados de preenchimento ou conteúdo demonstrativo explicitamente identificado, jamais conteúdo fictício tratado como verdadeiro.

## 2. STACK OBRIGATÓRIA

### Front-end
- React.
- TypeScript com tipagem rigorosa.
- Vite.
- React Router.
- Tailwind CSS.
- Biblioteca de componentes acessíveis e personalizáveis, preferencialmente shadcn/ui ou alternativa equivalente.
- TanStack Query para sincronização de dados com a API.
- React Hook Form e Zod para formulários e validações.
- Biblioteca de ícones Lucide.
- Biblioteca de internacionalização para PT-BR e EN.

### Back-end
- Java LTS compatível com a versão escolhida do Spring Boot.
- Spring Boot em versão estável e suportada.
- Spring Web.
- Spring Security.
- Spring Data JPA.
- Bean Validation.
- Flyway para migrações de banco.
- PostgreSQL.
- OpenAPI/Swagger para documentação da API.
- JUnit e testes de integração.

### Infraestrutura
- Docker Compose para desenvolvimento local.
- Vercel para o front-end.
- API Java hospedável em Railway, Render ou outro ambiente compatível.
- PostgreSQL gerenciado em produção.
- Armazenamento de arquivos em nuvem compatível com S3 ou alternativa adequada.
- GitHub Actions para CI.

Utilize versões estáveis compatíveis entre si. Confirme nas documentações oficiais as versões e configurações antes de fixá-las.

Evite dependências desnecessárias e arquiteturas excessivamente complexas.

## 3. ARQUITETURA

Implementar um monorepositório contendo:

```text
safe-system/
├── apps/
│   ├── web/
│   └── api/
├── infra/
│   ├── docker/
│   └── deployment/
├── docs/
├── .github/
│   └── workflows/
├── docker-compose.yml
├── .env.example
└── README.md
```

O front-end deve separar:
- app/router;
- layouts;
- pages;
- components/ui;
- components/features;
- hooks;
- services/api;
- features;
- i18n;
- types;
- styles.

A API deve usar organização modular por domínio, com separação clara de responsabilidades:

- controllers;
- DTOs;
- services;
- repositories;
- entities;
- mappers;
- validation;
- security;
- exception handling;
- configuration.

Os módulos de negócio podem ser organizados como packages separados.

Regras:

1. Não colocar lógica de negócio nos controllers.
2. Não expor entidades JPA diretamente na API.
3. Não implementar camada genérica desnecessária.
4. Usar DTOs específicos para entrada e saída.
5. Tratar erros de forma centralizada.
6. Padronizar respostas paginadas.
7. Utilizar migrations versionadas.
8. Manter o domínio independente do provedor de armazenamento quando viável.
9. Configurações e segredos exclusivamente por variáveis de ambiente ou mecanismos seguros equivalentes.
10. Não criar microserviços. Um monólito modular é suficiente.

## 4. IDENTIDADE VISUAL

Nome do produto: SAFE System.

Conceito: uma plataforma pessoal de carreira com aparência de software premium.

Direção visual: híbrido entre minimalismo sofisticado e interface de sistema futurista.

O visual deve transmitir:
- Organização.
- Precisão.
- Competência técnica.
- Confiança.
- Sobriedade.
- Personalidade.

Não transformar a interface em um terminal hacker ou em um dashboard sobrecarregado.

### Tema escuro

- Background principal: `#0B0B10`.
- Background secundário: `#12121A`.
- Cards: `#171722`.
- Bordas: `#262637`.
- Roxo principal: `#7C3AED`.
- Roxo secundário: `#A855F7`.
- Texto principal: `#F3F4F6`.
- Texto secundário: `#A1A1B3`.

### Tema claro

Desenvolver uma paleta própria, com fundos claros, contraste adequado, bordas discretas e roxo como elemento de identidade.

Utilizar CSS variables e design tokens para suportar ambos os temas.

### Tipografia

- Inter ou Manrope para conteúdo.
- JetBrains Mono para labels técnicos, metadados, datas e indicadores.

### Componentes visuais

Criar componentes consistentes para:
- Header e navegação.
- Sidebar administrativa.
- Botões.
- Inputs.
- Selects.
- Textareas.
- Modais.
- Confirmação de ações destrutivas.
- Cards de projetos.
- Cards de certificados.
- Badges.
- Tags.
- Tabelas.
- Abas.
- Tooltips.
- Toasts.
- Skeleton loaders.
- Estados vazios.
- Mensagens de erro.
- Breadcrumbs.
- Filtros e paginação.

### Interações

- Hover sutil.
- Bordas e estados de foco visíveis.
- Transições rápidas.
- Glow roxo muito discreto.
- Suporte a `prefers-reduced-motion`.
- Sem animações desnecessárias.
- Sem efeitos que prejudiquem a legibilidade.

### Responsividade

Mobile-first.

Layouts adequados para:
- Celulares.
- Tablets.
- Desktops.
- Telas largas.

Não permitir overflow horizontal acidental.

## 5. IDIOMAS E TEMAS

Toda a área pública deve funcionar em português brasileiro e inglês.

Requisitos:

- PT-BR como idioma padrão.
- Botão para alternar PT/EN.
- Preferência preservada entre visitas.
- Nenhum texto estrutural hardcoded fora do sistema i18n.
- Conteúdos públicos importantes devem permitir título e descrição em ambos os idiomas.
- Fallback explícito e consistente quando faltar tradução de conteúdo.
- Não traduzir automaticamente nomes próprios, empresas e tecnologias.
- Datas formatadas conforme idioma.

Tema:
- Dark como padrão do produto.
- Light disponível.
- Alternância manual.
- Preferência persistida.
- Evitar flash de tema incorreto ao carregar.

O painel administrativo pode usar PT-BR como idioma inicial, mas sua arquitetura deverá suportar internacionalização.

## 6. EXPERIÊNCIA PÚBLICA

Criar as seguintes rotas lógicas, respeitando o prefixo de idioma selecionado ou uma estratégia equivalente de URLs localizadas:

```text
/
 /projects
 /projects/:slug
 /certificates
 /certificates/:slug
 /experience
 /skills
 /resume
 /contact
```

O sistema deverá preservar o idioma ao navegar e suportar acesso direto a cada rota.

### 6.1 HOME — CORE PROFILE

A página inicial deve conter:

**Hero**
- Nome profissional.
- Título profissional editável.
- Bio curta.
- Localização profissional.
- Disponibilidade editável.
- Links para GitHub, LinkedIn e e-mail.
- CTA "Explorar projetos".
- CTA "Ver currículo".
- CTA "Certificados".

**Resumo de competências**
- Tecnologias principais.
- Áreas profissionais.
- Cards de destaque.

**Projetos destacados**
- Seleção manual pelo administrador.
- Cards clicáveis.
- Nome, descrição, tecnologias e imagem.
- Link para detalhes.

**Experiências**
- Resumo de experiências selecionadas.
- Timeline compacta.

**Certificados recentes**
- Certificados publicados.
- Links para páginas individuais.

**Contato**
- Links profissionais.
- CTA para envio de e-mail.

Não utilizar formulário que simule envio de mensagem sem possuir um serviço real de entrega.

### 6.2 PROJECT INDEX

Página de projetos com:

- Busca textual.
- Filtros por categoria.
- Filtros por tecnologia.
- Filtros por status.
- Ordenação por relevância e data.
- Paginação ou carregamento incremental.
- Cards responsivos.

Categorias iniciais:
- Back-end.
- Front-end.
- Full stack.
- Automação.
- Infraestrutura.
- Acadêmico.

Detalhes de cada projeto:

- Nome.
- Resumo.
- Descrição completa.
- Problema abordado.
- Solução desenvolvida.
- Minha contribuição.
- Tecnologias.
- Arquitetura técnica.
- Desafios.
- Aprendizados.
- Resultados comprovados, quando existirem.
- Galeria de imagens.
- Repositório GitHub.
- Demonstração ao vivo, quando houver.
- Status do projeto.
- Datas relevantes.

As URLs dos projetos devem usar slugs únicos.

Não exibir links inexistentes.

### 6.3 CERTIFICATE VAULT

Biblioteca pública de certificados.

Funcionalidades:

- Busca por nome.
- Filtro por instituição.
- Filtro por área.
- Filtro por competência.
- Ordenação por data.
- Grid e visualização responsiva.

Cada certificado terá:
- Título.
- Instituição emissora.
- Data de emissão.
- Data de expiração, se aplicável.
- Carga horária opcional.
- Descrição.
- Competências.
- Documento PDF.
- URL de validação opcional.
- Identificador de credencial opcional.
- Status de publicação.

Página de detalhes:
- Metadados.
- Preview do certificado.
- Botão para abrir documento.
- Link de validação, se disponível.
- Download quando permitido.

Documentos privados jamais poderão ser acessados por endpoints públicos.

### 6.4 PROFESSIONAL TIMELINE

Exibir experiências profissionais e acadêmicas relevantes.

Cada experiência terá:
- Cargo ou função.
- Organização.
- Modalidade.
- Período.
- Descrição.
- Responsabilidades.
- Tecnologias.
- Resultados verificáveis.
- Imagens ou links opcionais.
- Indicação de experiência atual, quando aplicável.

Visualizar experiências numa timeline elegante e acessível.

### 6.5 SKILL MATRIX

Organizar competências em grupos:

- Linguagens.
- Frameworks.
- Bancos de dados.
- Desenvolvimento web.
- Infraestrutura e redes.
- Ferramentas.
- Competências interpessoais.

Não usar porcentagens arbitrárias de domínio.

Cada competência deve poder ser vinculada aos projetos, certificados ou experiências que a demonstram.

### 6.6 RESUME EXPORT

O proprietário escolheu o modelo de currículo por UPLOAD.

Não implementar geração dinâmica de currículo nesta versão.

Funcionalidades:

- Administrador envia arquivos PDF de currículo.
- Pode cadastrar diversas versões.
- Cada versão possui título, idioma, descrição e perfil-alvo.
- Pode ativar ou desativar disponibilidade pública.
- Pode definir uma versão principal para PT-BR e outra para EN.
- Visitantes podem visualizar ou baixar apenas versões publicadas.
- Documentos privados não ficam acessíveis ao público.

### 6.7 CONTATO

- E-mail profissional configurável.
- GitHub.
- LinkedIn.
- Localização profissional.
- Links sociais autorizados.
- Botão de copiar e-mail.
- Links funcionando corretamente.

## 7. PERFIS PROFISSIONAIS

Implementar perfis de apresentação:

- Suporte e Infraestrutura.
- Desenvolvimento.
- Segurança da Informação.

Cada perfil pode possuir:
- Título.
- Resumo profissional.
- Competências em destaque.
- Projetos em destaque.
- Currículo PDF associado.

O visitante poderá alternar entre perfis sem perder a navegação.

A mudança de perfil reorganiza os destaques, sem inventar habilidades ou esconder permanentemente outras experiências.

O administrador define o conteúdo de cada perfil.

## 8. PAINEL ADMINISTRATIVO

Rotas:

```text
/admin/login
/admin
/admin/projects
/admin/certificates
/admin/experiences
/admin/skills
/admin/resumes
/admin/learning
/admin/applications
/admin/analytics
/admin/profiles
/admin/settings
```

Todas, exceto `/admin/login`, exigem autenticação e autorização no servidor.

### 8.1 DASHBOARD

Exibir indicadores reais:
- Projetos cadastrados.
- Projetos publicados.
- Certificados cadastrados.
- Certificados publicados.
- Cursos em andamento.
- Candidaturas em andamento.
- Total de visitas, quando analytics estiver configurado.

Adicionar:
- Atividades recentes.
- Ações rápidas.
- Projetos em rascunho.
- Cursos pendentes.
- Candidaturas com próximas ações.

Não inventar dados para preencher gráficos.

### 8.2 GERENCIAMENTO DE PROJETOS

CRUD completo:
- Criar.
- Editar.
- Excluir.
- Publicar.
- Despublicar.
- Marcar como destaque.
- Reordenar.
- Vincular competências.
- Vincular imagens.
- Cadastrar conteúdo bilíngue.

Suportar rascunhos.

### 8.3 GERENCIAMENTO DE CERTIFICADOS

CRUD completo com:
- Upload PDF.
- Metadados.
- Tags.
- Instituição.
- Competências.
- Visibilidade.
- Preview.
- Exclusão segura.

Separar dados do certificado do objeto armazenado na nuvem.

### 8.4 EXPERIÊNCIAS E COMPETÊNCIAS

CRUD completo para experiências, competências e vínculos.

Utilizar relacionamentos adequados, sem duplicação desnecessária.

### 8.5 GESTÃO DE CURSOS E ESTUDOS

Módulo privado chamado Learning Center.

Entidade de curso:
- Nome.
- Plataforma ou instituição.
- Categoria.
- URL.
- Status: planejado, em andamento, concluído, pausado.
- Data de início.
- Data de conclusão.
- Progresso opcional.
- Observações.
- Competências relacionadas.
- Certificado relacionado, caso exista.

Funcionalidades:
- Criar e editar cursos.
- Atualizar progresso.
- Filtrar por status.
- Relacionar curso com certificado.
- Visualizar cursos concluídos e em andamento.

Nenhum curso privado deve aparecer publicamente por padrão.

### 8.6 CONTROLE DE CANDIDATURAS

Módulo privado chamado Application Tracker.

Registrar:
- Empresa.
- Cargo.
- Link da vaga.
- Modalidade.
- Faixa salarial opcional.
- Data da candidatura.
- Status.
- Fonte da oportunidade.
- Currículo utilizado.
- Anotações.
- Próxima ação.
- Data de acompanhamento.

Status iniciais:
- Interesse.
- Candidatura enviada.
- Triagem.
- Entrevista.
- Teste técnico.
- Proposta.
- Contratado.
- Rejeitado.
- Desistência.

Funcionalidades:
- CRUD.
- Filtros por status.
- Ordenação por data.
- Visão Kanban e tabela.
- Histórico de mudanças de etapa.
- Estatísticas reais de candidaturas.
- Próximas ações pendentes.

Não implementar scraping de vagas nem candidaturas automáticas.

Esse módulo é estritamente privado, sem exposição na API pública.

### 8.7 ANALYTICS

Criar visualização administrativa contendo:
- Visualizações de páginas.
- Projetos mais acessados.
- Certificados mais acessados.
- Downloads de currículo.
- Evolução temporal das visitas.

Preferir uma solução de analytics respeitosa à privacidade e compatível com LGPD.

Usar integração configurável ou eventos agregados sem identificação pessoal, quando viável.

Não coletar dados pessoais desnecessários, nem armazenar IP completo como mecanismo padrão.

Não expor chaves privadas no front-end.

Se depender de serviço externo ainda não configurado, exibir estado de configuração pendente, sem apresentar dados falsos.

### 8.8 CONFIGURAÇÕES

Permitir editar:
- Nome profissional.
- Bio.
- Títulos profissionais.
- Localização.
- Links sociais.
- E-mail.
- Disponibilidade.
- Destaques da Home.
- Idioma padrão.
- Currículos principais.
- Metadados de SEO.
- Preferências de publicação.

## 9. INTEGRAÇÃO COM GITHUB

Implementar integração somente para leitura.

Objetivo:
- Exibir repositórios públicos selecionados.
- Consultar dados relevantes de projetos.
- Facilitar acesso ao código.

Regras:
- Preferir links manuais como fonte principal.
- Integração não pode bloquear a aplicação caso GitHub esteja indisponível.
- Utilizar cache e respeitar limites da API.
- Não publicar repositórios privados.
- Não depender de métricas de estrelas ou commits para avaliar competência profissional.
- Se houver token de GitHub, mantê-lo apenas no servidor.
- Não importar automaticamente todos os repositórios para o portfólio.

O administrador controla quais repositórios estão associados a projetos publicados.

## 10. BUSCA E FILTROS

Implementar filtros reais com estado sincronizado à URL sempre que adequado.

Requisitos:
- Busca insensível a maiúsculas/minúsculas.
- Paginação.
- Ordenação.
- Filtros combináveis.
- Estados vazios claros.
- Botão para limpar filtros.
- Debounce quando necessário.
- Consultas eficientes no servidor.

Não usar dados estáticos no front-end para simular busca após a API estar implementada.

## 11. MODELO DE DADOS

Propor e implementar um esquema PostgreSQL normalizado, com migrações Flyway.

Entidades mínimas:

**admin_users**
- id
- username
- password_hash
- enabled
- created_at
- updated_at

**projects**
- id
- slug
- category
- status
- github_url
- demo_url
- featured
- published
- display_order
- started_at
- completed_at
- created_at
- updated_at

**project_translations**
- project_id
- locale
- title
- summary
- description
- problem
- solution
- contribution
- challenges
- learnings
- results

**certificates**
- id
- issuer
- category
- issued_at
- expires_at
- hours
- credential_id
- verification_url
- file_id
- published
- created_at
- updated_at

**certificate_translations**
- certificate_id
- locale
- title
- description

**experiences**
- id
- organization
- type
- started_at
- ended_at
- current
- published
- display_order

**experience_translations**
- experience_id
- locale
- role
- description
- responsibilities
- results

**skills**
- id
- slug
- category
- display_order
- published

**skill_translations**
- skill_id
- locale
- name
- description

**resumes**
- id
- title
- locale
- target_profile
- description
- file_id
- is_primary
- published
- created_at

**courses**
- id
- name
- provider
- category
- source_url
- status
- progress
- started_at
- completed_at
- notes
- certificate_id

**job_applications**
- id
- company
- position
- job_url
- work_mode
- salary_min
- salary_max
- currency
- applied_at
- status
- source
- resume_id
- notes
- next_action
- follow_up_at
- created_at
- updated_at

**application_events**
- id
- application_id
- previous_status
- new_status
- notes
- occurred_at

**files**
- id
- storage_key
- original_name
- mime_type
- size_bytes
- checksum
- visibility
- created_at

**professional_profiles**
- id
- slug
- display_order
- enabled
- default_resume_id

**profile_translations**
- profile_id
- locale
- title
- summary

**site_settings**
- key
- value
- updated_at

Adicionar tabelas de relacionamento para:
- Project/Skill.
- Certificate/Skill.
- Experience/Skill.
- ProfessionalProfile/Project.
- ProfessionalProfile/Skill.
- Media/Project.

Para analytics, utilizar tabelas de eventos agregados ou integração externa, justificando a escolha.

Regras:
- UUIDs para IDs, quando adequado.
- Foreign keys.
- Índices nas consultas importantes.
- Constraints.
- Unicidade de slug.
- Consistência de publicação.
- Datas auditáveis.
- Política clara de exclusão.
- Evitar campos duplicados e arrays não estruturados para relacionamentos relevantes.

O esquema apresentado é uma base conceitual: refine onde necessário, preservando todos os requisitos.

## 12. API REST

Prefixo: `/api/v1`.

### Endpoints públicos

```text
GET /public/profile
GET /public/professional-profiles
GET /public/projects
GET /public/projects/{slug}
GET /public/certificates
GET /public/certificates/{slug}
GET /public/experiences
GET /public/skills
GET /public/resumes
GET /public/resumes/{id}/download
GET /public/github/repositories
GET /public/settings
```

Criar query parameters para:
- page
- size
- sort
- search
- category
- skill
- locale
- professionalProfile

Não aceitar ordenações arbitrárias que sejam convertidas diretamente em SQL.

### Autenticação

```text
POST /auth/login
POST /auth/logout
GET  /auth/me
GET  /auth/csrf
```

Adaptar o endpoint CSRF à abordagem de sessão adotada.

### Endpoints administrativos

```text
GET    /admin/dashboard

GET    /admin/projects
POST   /admin/projects
GET    /admin/projects/{id}
PUT    /admin/projects/{id}
DELETE /admin/projects/{id}
PATCH  /admin/projects/{id}/publication

GET    /admin/certificates
POST   /admin/certificates
GET    /admin/certificates/{id}
PUT    /admin/certificates/{id}
DELETE /admin/certificates/{id}

GET    /admin/experiences
POST   /admin/experiences
PUT    /admin/experiences/{id}
DELETE /admin/experiences/{id}

GET    /admin/skills
POST   /admin/skills
PUT    /admin/skills/{id}
DELETE /admin/skills/{id}

GET    /admin/resumes
POST   /admin/resumes
PUT    /admin/resumes/{id}
DELETE /admin/resumes/{id}

GET    /admin/courses
POST   /admin/courses
PUT    /admin/courses/{id}
DELETE /admin/courses/{id}

GET    /admin/applications
POST   /admin/applications
GET    /admin/applications/{id}
PUT    /admin/applications/{id}
DELETE /admin/applications/{id}
POST   /admin/applications/{id}/events

GET    /admin/analytics
GET    /admin/profiles
POST   /admin/profiles
PUT    /admin/profiles/{id}
DELETE /admin/profiles/{id}

GET    /admin/settings
PUT    /admin/settings

POST   /admin/files
GET    /admin/files/{id}
DELETE /admin/files/{id}
```

Complementar endpoints específicos para publicação, ordenação, mídias, vínculos e download seguro, conforme necessário.

Implementar convenções consistentes e documentar a API.

### Contratos

- JSON padronizado.
- Paginação consistente.
- DTOs tipados.
- HTTP status corretos.
- Validation errors legíveis.
- Identificadores consistentes.
- Datas em ISO 8601.
- Sem vazamento de stack traces.
- OpenAPI atualizado.

## 13. AUTENTICAÇÃO E SEGURANÇA

O painel pertence a um único administrador.

Implementar:
- Spring Security.
- Autenticação por sessão de servidor, com cookie HttpOnly, Secure em produção e SameSite apropriado ao ambiente.
- Sessões com expiração e invalidação no logout.
- Proteção CSRF para ações autenticadas.
- CORS restrito às origens configuradas.
- HTTPS obrigatório em produção.
- Password hashing com algoritmo adequado como Argon2id ou BCrypt.
- Rate limiting ou proteção equivalente contra tentativas repetidas de login.
- Proteção contra enumeração de usuário.
- Configuração segura de headers HTTP.
- Validação server-side.
- Controle de acesso por endpoint.
- Transações quando necessárias.

Preferir hospedar front-end e API sob o mesmo site registrável, por exemplo `www.dominio.com` e `api.dominio.com`, ou utilizar um proxy seguro, para simplificar política de cookies.

Não usar `localStorage` para guardar credenciais, tokens ou sessões.

Não criar senha de administrador padrão em produção.

Implementar processo de provisionamento inicial via variáveis de ambiente seguras ou comando administrativo documentado.

Em desenvolvimento, permitir criar uma conta de demonstração somente por configuração explícita e isolada, nunca habilitada automaticamente em produção.

### Upload de arquivos

- Aceitar somente formatos explicitamente permitidos.
- PDFs para currículos e certificados.
- JPG, PNG e WebP para imagens.
- Validar tamanho, extensão, tipo real e assinatura quando aplicável.
- Gerar nomes internos únicos.
- Não confiar no nome original.
- Impedir path traversal e execução de arquivos.
- Usar bucket privado por padrão.
- URLs assinadas e expiração para acesso autorizado.
- Servir publicamente apenas arquivos vinculados a conteúdo publicado e autorizado.
- Considerar verificação antimalware ou mecanismo equivalente quando disponível.
- Tratar falhas de upload e exclusão.
- Não permitir que um documento privado seja acessado simplesmente por conhecer seu ID.
- Documentar limites e riscos residuais.

### Segurança de dados

- Não registrar senhas ou cookies de sessão em logs.
- Não registrar notas privadas de candidaturas em ferramentas de analytics.
- Evitar coleta desnecessária de dados pessoais.
- Implementar exclusão de dados com comportamento documentado.
- Proteger segredos e credenciais externas.
- Não commitar `.env` real.

## 14. SEO, ACESSIBILIDADE E PERFORMANCE

### SEO

- Titles por página.
- Meta descriptions.
- Open Graph.
- Canonical URLs.
- Sitemap XML.
- robots.txt.
- Hreflang ou estratégia equivalente para idiomas.
- URLs amigáveis.
- Metadados de projetos.

Garantir indexação adequada da área pública.

Como React/Vite com renderização exclusivamente no cliente pode limitar o SEO, avaliar uma estratégia de pré-renderização das páginas públicas relevantes, sem abandonar a stack aprovada.

Nunca indexar rotas administrativas.

### Acessibilidade

- HTML semântico.
- Navegação por teclado.
- Labels para formulários.
- Contraste adequado.
- Focus states.
- Estados de erro acessíveis.
- Suporte a leitores de tela.
- Textos alternativos.
- Respeito à redução de movimento.
- Dialogs e menus acessíveis.

Alvo: WCAG 2.2 AA nos fluxos principais.

### Performance

- Code splitting.
- Lazy loading de páginas e imagens.
- Cache inteligente.
- Evitar consultas N+1 no JPA.
- Paginação no servidor.
- Compressão e otimização de imagens.
- Bundle de produção otimizado.
- Evitar requisições redundantes.

## 15. TESTES

### Back-end

Criar testes para:
- Serviços de negócio.
- Validações.
- Repositórios críticos.
- Endpoints públicos.
- Endpoints administrativos.
- Autorização.
- Autenticação.
- CSRF.
- Upload de arquivos.
- Publicação/ocultação.
- Integridade de dados.

Preferir Testcontainers para testes reais de integração com PostgreSQL quando o ambiente permitir.

### Front-end

Criar testes para:
- Componentes críticos.
- Formulários.
- Filtros.
- Troca de idioma.
- Troca de tema.
- Estados vazios e erros.
- Navegação.

Criar fluxos end-to-end com Playwright para:
1. Login do administrador.
2. Cadastro de projeto.
3. Publicação do projeto.
4. Acesso público ao projeto.
5. Upload de certificado.
6. Controle de visibilidade.
7. Cadastro de candidatura.
8. Atualização de curso.
9. Download de currículo.
10. Logout.

Não apenas escrever testes: executar os que o ambiente permitir e relatar os resultados reais.

## 16. DEVOPS E DEPLOY

Criar:
- Dockerfile da API.
- Docker Compose local.
- PostgreSQL local.
- Migrações automáticas controladas.
- `.env.example`.
- Configuração de build do front-end.
- Configuração de SPA rewrites para Vercel.
- Healthcheck da API.
- GitHub Actions com build, lint e testes.

Separar ambientes:
- Local.
- Desenvolvimento/staging, quando necessário.
- Produção.

Variáveis esperadas incluem:
- DATABASE_URL ou parâmetros equivalentes.
- Credenciais de banco.
- SESSION_CONFIG.
- CORS_ALLOWED_ORIGINS.
- FRONTEND_URL.
- STORAGE_ENDPOINT.
- STORAGE_BUCKET.
- STORAGE_ACCESS_KEY.
- STORAGE_SECRET_KEY.
- GITHUB_TOKEN opcional.
- Analytics config opcional.

Não publicar segredos usando variáveis de front-end.

Documentar configurações de domínio, HTTPS, cookies e CORS.

Documentar backups e restauração do PostgreSQL e do armazenamento de arquivos.

Priorizar soluções compatíveis com planos gratuitos ou de baixo custo, sem pressupor gratuidade permanente.

## 17. DOCUMENTAÇÃO OBRIGATÓRIA

Criar:

**README.md**
- O que é o SAFE System.
- Stack.
- Arquitetura.
- Requisitos locais.
- Como executar.
- Como testar.
- Variáveis de ambiente.
- Screenshots, quando disponíveis.
- Como fazer deploy.

**docs/ARCHITECTURE.md**
- Diagrama de arquitetura.
- Decisões arquiteturais.
- Organização do repositório.
- Fluxo de autenticação.
- Fluxo de arquivos.
- Modelo de dados.

**docs/API.md**
- Endpoints.
- Exemplos de requests.
- Exemplos de responses.
- Autenticação.
- Códigos de erro.

**docs/DEPLOYMENT.md**
- Vercel.
- API Java.
- PostgreSQL.
- Storage.
- Domínio.
- Ambiente de produção.
- Segurança.

**docs/DEVELOPMENT.md**
- Padrões.
- Convenções.
- Testes.
- Scripts.
- Migrações.
- Como cadastrar conteúdo.

**docs/ROADMAP.md**
- Fases.
- Funcionalidades concluídas.
- Pendências.
- Melhorias futuras.

## 18. PLANO DE IMPLEMENTAÇÃO

O projeto completo é o escopo solicitado, mas sua implementação deve ser incremental, com checkpoints verificáveis.

### FASE 0 — Auditoria do ambiente e planejamento
- Inspecionar o repositório.
- Identificar arquivos existentes.
- Confirmar versões de ferramentas.
- Documentar arquitetura proposta.
- Criar checklist detalhado.
- Não sobrescrever trabalho existente sem necessidade.

### FASE 1 — Fundação
- Configurar monorepo.
- Criar aplicação React.
- Criar API Spring Boot.
- Configurar PostgreSQL.
- Criar Docker Compose.
- Configurar migrations.
- Configurar linters.
- Configurar testes e CI básicos.
- Garantir inicialização local.

### FASE 2 — Design system e front-end público
- Tokens.
- Dark/light.
- i18n.
- Layout.
- Home.
- Projetos.
- Certificados.
- Experiências.
- Competências.
- Currículos.
- Contato.
- Responsividade.
- Estados vazios.

### FASE 3 — API pública e banco
- Entidades.
- DTOs.
- Repositórios.
- Serviços.
- Controllers.
- Paginação.
- Filtros.
- Conteúdo bilíngue.
- Integração real do front-end com a API.

### FASE 4 — Autenticação e painel administrativo
- Provisionamento de admin.
- Login.
- Logout.
- Proteção de rotas.
- Dashboard.
- CRUD de projetos.
- CRUD de certificados.
- CRUD de experiências.
- CRUD de competências.
- Upload seguro.
- Publicação.

### FASE 5 — Módulos profissionais
- Gerenciamento de currículos.
- Perfis profissionais.
- Learning Center.
- Application Tracker.
- Histórico de candidaturas.

### FASE 6 — Integrações e métricas
- Integração GitHub.
- Analytics real.
- Indicadores administrativos.
- Configurações de integração.
- Tratamento de indisponibilidade de serviços externos.

### FASE 7 — Qualidade e publicação
- Testes automatizados.
- Revisão de acessibilidade.
- Revisão de segurança.
- Performance.
- SEO.
- CI.
- Docker.
- Configuração de produção.
- Documentação final.
- Checklist de deploy.

Em cada fase:
1. Implementar o código completo daquela etapa.
2. Executar build e testes pertinentes.
3. Corrigir problemas encontrados.
4. Atualizar o roadmap.
5. Informar arquivos modificados e funcionalidades entregues.
6. Declarar pendências e bloqueios reais.

Não tratar itens pendentes como concluídos.

## 19. CRITÉRIOS DE ACEITE

O SAFE System estará concluído quando:

- A aplicação iniciar localmente seguindo o README.
- PostgreSQL funcionar com migrations versionadas.
- O front-end consumir dados reais da API.
- Projetos puderem ser criados, editados, publicados e ocultados.
- Certificados puderem receber PDFs reais.
- Arquivos privados estiverem protegidos.
- Currículos enviados pelo admin puderem ser acessados publicamente quando publicados.
- Perfis profissionais reorganizarem os destaques.
- PT-BR e EN funcionarem.
- Tema dark e light funcionarem.
- Login e logout funcionarem.
- Rotas administrativas exigirem autenticação.
- Learning Center tiver CRUD funcional.
- Application Tracker tiver CRUD e histórico funcional.
- A busca e os filtros funcionarem com dados reais.
- GitHub possuir integração funcional ou estado de configuração explicitamente documentado.
- Analytics usar dados reais ou informar claramente a ausência da integração.
- As páginas forem responsivas.
- Fluxos principais forem navegáveis por teclado.
- Os testes relevantes forem executados sem falhas conhecidas não documentadas.
- Os scripts de build funcionarem.
- O sistema possuir documentação de deploy.
- Nenhum segredo estiver versionado.
- Não existirem botões falsos, páginas placeholder apresentadas como concluídas ou métricas fabricadas.

Serviços externos dependentes de credenciais devem ter implementação e configuração documentadas, mas a integração só poderá ser declarada operacional após teste real.

## 20. REGRAS DE EXECUÇÃO PARA O CODEX

Estas instruções são obrigatórias:

1. Implemente código real, não apenas pseudocódigo.
2. Não substitua o escopo por uma landing page.
3. Não elimine módulos silenciosamente.
4. Não crie endpoints sem comportamento correspondente.
5. Não use dados inventados como conteúdo profissional verdadeiro.
6. Não implemente funcionalidades puramente decorativas.
7. Não coloque segredos no repositório.
8. Não utilize arquitetura de microserviços.
9. Prefira soluções simples, seguras e sustentáveis.
10. Siga padrões modernos de React, TypeScript e Spring Boot.
11. Documente decisões técnicas importantes.
12. Garanta consistência entre front-end, API e banco.
13. Antes de declarar uma etapa concluída, valide build e testes.
14. Se algo exigir credenciais externas, implemente a integração configurável e documente exatamente o que falta.
15. Se alguma decisão técnica relevante estiver indefinida, escolha uma opção tecnicamente sólida, explique brevemente e continue.
16. Não solicite confirmação para cada pequena decisão.
17. Preserve o escopo completo no roadmap.
18. Priorize um produto realmente utilizável.
19. Use o painel privado como fonte da verdade para os dados públicos.
20. Nunca alegue deploy, execução de teste ou integração funcionando sem verificação.

## 21. PRIMEIRA AÇÃO

Comece inspecionando o workspace atual.

Depois:

1. Apresente um resumo da arquitetura escolhida.
2. Defina a estrutura inicial do repositório.
3. Crie o roadmap completo.
4. Implemente imediatamente a FASE 1.
5. Execute as validações possíveis.
6. Continue para a próxima fase quando a anterior estiver estável e houver contexto e recursos disponíveis.
7. Ao final de cada etapa, apresente um resumo objetivo com o que foi realmente implementado e o que permanece pendente.

O objetivo final é entregar um SAFE System utilizável, bonito, seguro e tecnicamente defensável como projeto full stack profissional.

**Construa o sistema. Não apenas descreva como ele seria construído.**