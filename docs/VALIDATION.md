# Evidências de validação

Validação no ambiente de desenvolvimento em 14/10/2026. Nenhuma publicação externa ou restauração em nova tarefa é inferida desses resultados.

| Check | Resultado |
|---|---|
| Java / Maven | JDK 21 completo e Maven 3.9.11 via wrapper, distribuição validada com SHA-256 |
| API `verify` | 17 testes, zero falhas, zero erros, zero skipped; PostgreSQL real `safe_test` |
| Front-end Vitest | 14 testes, três arquivos, todos passaram |
| TypeScript / Vite | Build de produção passou, rotas divididas em chunks |
| ESLint | Passou sem erros ou warnings na revisão final |
| Playwright | Quatro jornadas passaram; dez fluxos solicitados, idioma/tema/mobile/teclado, acessibilidade, vínculos, perfis, settings, analytics e OpenAPI |
| Modo local com Docker | Compose iniciou PostgreSQL + sistema em volumes separados; páginas, login/CSRF e upload/download PDF passaram; reinício preservou administrador e arquivo. Validado no executor Linux; script PowerShell preparado, sem execução em Windows neste ambiente. |
| Hospedagem combinada | Imagem `hosted.Dockerfile` construída; Caddy + API executados como usuário `safe`; health UP, páginas PT/EN/login HTTP 200, API pública JSON 200 e admin anônimo 401. Teste local, sem deploy no Railway. |
| Docker | Imagem construída e API iniciada com PostgreSQL em rede isolada; health `healthy`, perfil/lista HTTP 200, admin anônimo HTTP 401; UID 10001 |
| Instalação cloud | Script de instalação executado, wrapper e `npm ci`/build confirmados |
| Startup cloud | Readiness por request de health, perfil e projetos passou; repetição preservou PID e configuração local. Banco de desenvolvimento com zero projetos e três perfis iniciais |
| Pré-renderização | 14 páginas públicas PT-BR/EN geradas no banco de desenvolvimento, com HTML localizado; nenhum conteúdo administrativo |
| npm audit de produção | Zero vulnerabilidades reportadas na execução registrada; não equivale a auditoria de segurança completa |

## Cobertura e contexto

API: autenticação/logout, CSRF omitido, autorização, publicação/ocultação, busca e paginação, sort inválido, slugs duplicados, URL insegura, PDFs privados, bloqueio de exclusão vinculada, rejeição de conteúdo ativo/path traversal, histórico de candidatura, progresso de curso, currículo principal/idioma, CORS, fila de exclusão e ação Launch identificada no objeto PDF parseado.

Web: respostas vazias/erros, filtros em URL, formulários, transporte CSRF, preferências de tema, idioma e fallback de tradução parcial. Playwright usa API e PostgreSQL reais em `safe_e2e`, com arquivos PDF estruturalmente válidos. Registros E2E são identificados e ficam separados do banco de desenvolvimento `safe`.

A análise axe-core não encontrou violações nas páginas e critérios executados. Isso não certifica todo o produto como WCAG AA; auditoria manual completa permanece no roadmap. Falhas iniciais de setup, imports, seleção de testes e sidebar foram corrigidas; os resultados acima descrevem os checks finais, não tentativas anteriores.

## Fora do alcance dessa validação

GitHub Actions passou nas execuções iniciais dos commits `093bd02` e `fec998e`. Atualizações seguintes possuem suas próprias execuções, disponíveis na aba Actions. Não houve deploy Vercel/Railway/Render, bucket S3 de produção, domínio real, ensaio de restore no provedor, teste de carga ou pentest independente. A API GitHub possui seleção manual, cache assíncrono e fallback, mas nenhum enriquecimento externo de um projeto do proprietário é alegado como validado.

Canonical/hreflang/sitemap requerem `VITE_SITE_URL` real; não foram preenchidos com uma URL presumida. HTML pré-renderizado deve ser regenerado ao atualizar publicação e conteúdo. Volumes Docker/processos não são presumidos persistidos por uma nova snapshot de nuvem.

Os arquivos do aplicativo foram criados no checkout inicialmente vazio. Logs, credenciais locais, dumps e resultados estão ignorados pelo Git; não há senha de administrador padrão para produção. Revise a configuração da nuvem e publique a snapshot pelo produto para reutilizar o ambiente preparado.

`install_script` e `start_skill` foram persistidos no rascunho, junto à allowlist de documentação e `api.github.com`. Isso não executa scripts, não aplica rede em runtime e não publica a configuração. Revise/salve nas configurações e publique o ambiente para ativar a snapshot.
