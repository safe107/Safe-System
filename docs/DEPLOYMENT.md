# Publicação e operação

O projeto possui configurações de deploy. A publicação externa exige a conta de hospedagem do proprietário, PostgreSQL e armazenamento privado. Para publicar site e API no mesmo endereço com Railway, siga [PUBLISH-RAILWAY.md](PUBLISH-RAILWAY.md); `railway.toml` usa a imagem combinada, sem expor a API em uma segunda origem.

## Front-end / Vercel

Root directory: `apps/web`; install: `npm ci`; build: `npm run build`; output: `dist`. A configuração base em `infra/deployment/vercel.json` pode ser copiada para `apps/web/vercel.json` e ajustada ao domínio real.

Use `VITE_API_URL=https://api.seu-dominio` ou proxy same-origin; `VITE_SITE_URL=https://www.seu-dominio` fornece canonical/hreflang. Exemplos são ilustrativos, não URLs do proprietário. Nunca coloque token GitHub, credenciais S3 ou senhas em `VITE_*`.

Se quiser HTML indexável, rode também `npm run prerender` com `PRERENDER_API_URL` acessível ao build, Chromium e API saudável. Ele consulta apenas endpoints públicos. Regere e faça redeploy quando publicar/alterar conteúdo. Vercel deve servir os arquivos estáticos existentes antes do fallback SPA. Sitemap é criado apenas quando um domínio real está configurado; o build padrão tem robots que bloqueia `/admin`.

## API / Railway, Render ou host equivalente

Build da imagem: `docker build -f infra/docker/api.Dockerfile -t safe-api .`; execute com variáveis seguras do provedor. Java 21, porta via `PORT`, probe `/actuator/health`, armazenamento S3 privado e PostgreSQL gerenciado. Configure memória adequada ao heap, uploads e parsing; comece com uma instância e ajuste com medição real.

`DATABASE_URL` é JDBC (`jdbc:postgresql://…`), não a URI `postgres://…` de alguns provedores. `DATABASE_USERNAME` e `DATABASE_PASSWORD` são separados. Ative TLS de banco com validação de host/CA conforme o provedor (`sslmode=verify-full` e CA aprovada). Não configure `sslmode=disable` em produção.

Defina `SAFE_PRODUCTION=true`, `COOKIE_SECURE=true`, `FRONTEND_URL` HTTPS e `CORS_ALLOWED_ORIGINS` com origens exatas. A inicialização de produção rejeita cookie inseguro, front-end não HTTPS, armazenamento local, credencial de banco local conhecida e ausência de administrador. Use `ADMIN_USERNAME`/`ADMIN_PASSWORD` fortes apenas no provisionamento inicial e remova a senha do runtime após criação. BCrypt não permite reset por simples troca da variável: use um procedimento administrativo auditado no banco ou uma tarefa específica de recuperação, sem inventar rota pública de reset.

HTTPS deve ser obrigatório no ingress, com redirects de HTTP e HSTS. Não confie em Forwarded/X-Forwarded-* de qualquer cliente; só aceite headers do seu ingress conhecido. No modelo inicial, prefira `www` e `api` sob o mesmo site registrável. Cookie SameSite=Lax suporta esse desenho; domínios diferentes de Vercel/Render exigem proxy same-origin ou desenho de cookies revisado, evitando habilitar SameSite=None sem necessidade.

Sessões se perdem ao reiniciar a API. Para múltiplas réplicas, implemente Spring Session com store compartilhado antes de escalar. Rate limit em memória complementa, sem substituir, limites no gateway.

## Armazenamento

`STORAGE_MODE=s3`, `STORAGE_BUCKET`, `STORAGE_REGION`, opcional `STORAGE_ENDPOINT` HTTPS, `STORAGE_ACCESS_KEY` e `STORAGE_SECRET_KEY`, somente no servidor. Bucket privado, bloqueio de acesso público e IAM mínimo (Get/Put/Delete para o prefixo da aplicação). SDK não aplica ACL pública. URLs assinadas expiram em dois minutos e podem continuar válidas nesse intervalo após ocultação do conteúdo.

Use CORS do bucket apenas para visualização/download quando necessário e somente para o front-end real. Configure versionamento/lifecycle e política de retenção. Antimalware não está integrado; parsing não garante ausência de toda ameaça. Upload pertence somente ao administrador, não ao público.

A exclusão física usa uma fila persistente; monitore `file_deletion_jobs` e suas tentativas. Metadados removidos não têm endpoint de download. Não apague manualmente um objeto ainda referenciado no banco.

## GitHub e analytics

GitHub consulta `api.github.com` por HTTPS, somente URLs de projetos públicos escolhidos. Token é opcional e server-side; sem acesso, os links manuais continuam funcionando e `available=false` indica ausência de enriquecimento. O cache dura 15 minutos. Não há importação automática de repositórios nem avaliação por stars/commits.

Analytics interno é agregado e desativado por padrão; habilite no painel. Contadores não representam visitantes únicos; bots e eventos perdidos podem afetá-los. Não há armazenamento persistente de IP nem dados de candidaturas nos eventos. Defina política de retenção e disclosure compatíveis com o uso real.

## Backups e restauração

Banco: `pg_dump -Fc` usando credenciais por ambiente/provedor e arquivo seguro; armazene criptografado fora do servidor. Teste `pg_restore` em um banco novo e isolado. Faça backup antes de migrations. Restore deve incluir metadados de arquivos e uma versão consistente do bucket.

Arquivos: versionamento do bucket ou cópia criptografada periódica; no local, copie o volume `files` junto do dump. Não basta fazer backup do PostgreSQL. Registre RPO/RTO e ensaie restore com checks de links públicos/privados, login e downloads.

Antes do deploy: testes/build/lint, secrets seguros, HTTPS/CORS/cookies, bucket privado, limite de upload no ingress, migrations, healthcheck, backups/restore, custos/quota dos provedores, monitoramento, revisão de acessibilidade e segurança. Os resultados locais não comprovam deploy ou acesso ao bucket de produção.

## Build Docker no executor de nuvem

Neste executor, Maven precisa do proxy e CA do sistema. O Dockerfile aceita secrets de build `maven-settings` e `java-cacerts`; eles não são copiados à imagem final. O hostname do proxy precisa ser resolvido no contexto do builder, usando o endereço fornecido pelo ambiente. Não altere a allowlist para contornar isso e mantenha validação TLS. Em um host com acesso direto normal, o comando de build padrão funciona sem esses bindings.
