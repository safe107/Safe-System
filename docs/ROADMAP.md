# Roadmap SAFE System

Estados: [ ] pendente; [x] implementado e validado. Não representa deploy.

## Fase 0 — Auditoria
- [x] Inspecionar checkout vazio e preservar arquivos existentes.
- [x] Identificar Java 21, Node 24 e Docker; instalar Maven com SHA-512.
- [x] Confirmar compatibilidade pelo build e registrar arquitetura.

## Fase 1 — Fundação
- [x] Monorepo React/TypeScript/Vite e Spring Boot.
- [x] PostgreSQL, Flyway e constraints.
- [x] Compose, Dockerfile, ambiente e healthcheck.
- [x] Build, lint, testes e CI.
- [x] Inicialização funcional local.

## Fase 2 — Público e design system
- [x] Tokens dark/light, PT-BR/EN persistidos.
- [x] Layout mobile, navegação acessível e estados vazios/erros.
- [x] Home, projetos/detalhes, certificados/detalhes, experiências, competências, currículos e contato.
- [x] Perfis de apresentação e destaques.

## Fase 3 — API e banco
- [x] DTOs, entidades, serviços e repositórios por domínio.
- [x] Conteúdo bilíngue, vínculos normalizados e auditoria.
- [x] Busca, filtros combináveis, paginação e sort controlado.
- [x] Integração real front/API e documentação OpenAPI.

## Fase 4 — Segurança e administração
- [x] Provisionamento único admin explícito, sessão, CSRF, CORS e rate limit.
- [x] Login/logout, dashboard real e rotas protegidas.
- [x] CRUD projetos, certificados, experiências e competências.
- [x] Publicação, destaques, ordem e vínculos.
- [x] Upload validado, armazenamento privado, autorização por vínculo e exclusão segura.

## Fase 5 — Carreira
- [x] Currículos PDF, idiomas e principal por idioma.
- [x] Perfis profissionais com projetos/competências/currículo.
- [x] Learning Center: CRUD, status, progresso, certificado e competências.
- [x] Application Tracker: CRUD, tabela/Kanban, histórico e próximas ações.

## Fase 6 — Integrações
- [x] GitHub somente leitura, seleção manual, cache e falha não bloqueante.
- [x] Analytics agregado, sem IP/dados pessoais e sem números fictícios.
- [x] Configurações, indicadores reais e atividade recente.

## Fase 7 — Qualidade e publicação
- [x] Testes backend: login/sessão/CSRF/autorização/validação/publicação/arquivos/integridade.
- [x] Testes frontend: formulários/filtros/tema/idioma/erros/navegação.
- [x] Playwright: dez fluxos completos e vínculos/perfis/settings/analytics/OpenAPI.
- [x] Checks automatizados de acessibilidade, teclado, overflow mobile e code splitting.
- [ ] Auditoria manual completa WCAG 2.2 AA e medição de performance sob carga.
- [x] SEO por rota, robots e pré-renderização pública. Canonical/hreflang/sitemap são gerados quando VITE_SITE_URL está definido.
- [ ] Configurar e validar SEO com domínio real.
- [x] CI configurado, Docker build/runtime validado, configurações Vercel/API/PostgreSQL/S3 implementadas.
- [x] README, ARCHITECTURE, API, DEVELOPMENT, DEPLOYMENT, VALIDATION.
- [x] Procedimentos de backup/restauração e checklist de produção documentados.
- [ ] Ensaio de restore no provedor de produção e revisão independente de segurança.
- [x] install_script, start_skill e domínios necessários salvos no rascunho da nuvem para revisão.
- [ ] Publicação da snapshot e validação em uma nova tarefa.
- [ ] Deploy real e testes com serviços externos (dependem de credenciais/domínio).

## Futuro
Antimalware integrado, otimização de imagens, monitoramento, exportação privada de dados.

## Limites e acompanhamento

Os checks concluídos são de desenvolvimento local, sem alegação de publicação externa. S3 e GitHub estão implementados com configuração e estados de indisponibilidade; o acesso externo só é operacional após teste no destino real. Integração PostgreSQL foi executada em banco real dedicado, sem Testcontainers; migração do runner para Testcontainers é uma alternativa futura, não um teste executado.

Lookup de vínculos e algumas coleções de apoio carregam até 100 registros; índices e tabelas principais são paginados no servidor. Para catálogos maiores, adicionar pesquisa/paginação nos seletores de vínculos e nas coleções auxiliares. Antimalware, otimização/reprocessamento de imagens e retenção automática de analytics permanecem melhorias de produção.
