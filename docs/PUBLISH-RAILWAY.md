# Colocar o SAFE System online com Railway e Vercel

O código no GitHub não cria um site sozinho. Você pode publicar **todo o sistema no Railway** e obter um único link. Usar a Vercel para o front-end é opcional. Conecte suas contas diretamente nos provedores; não envie tokens ou senhas pelo chat. Verifique preços de serviço, banco e armazenamento antes de criar recursos.

## 1. Preparar banco e arquivos

Crie um PostgreSQL no seu projeto Railway, ou use PostgreSQL gerenciado que ofereça TLS validado. Guarde usuário, senha, hostname, porta e nome do banco no painel do provedor.

A aplicação recebe `DATABASE_URL` em formato **JDBC**, e usuário/senha em variáveis separadas. Não copie uma URI `postgresql://usuario:senha@host/banco` diretamente. Para uma conexão com certificado assinado por CA pública e hostname validado, o formato é:

```text
jdbc:postgresql://HOST:PORTA/BANCO?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory
```

Use os dados reais e confirme com seu provedor quais endpoint/certificado suportam TLS. Um hostname privado ou proxy TCP não garante certificado compatível. Se a imagem de PostgreSQL escolhida não fornecer TLS, configure TLS/certificados ou escolha um banco gerenciado com TLS; não desative a validação para contornar a falha. Para CA privada, configure a CA indicada pelo provedor.

Crie um bucket **S3 privado** em AWS ou provedor compatível, com bloqueio de acesso público, backup e credenciais restritas a ler/gravar/excluir objetos do bucket. A aplicação em produção usa esse bucket para PDFs e imagens; o disco efêmero do Railway não serve como armazenamento persistente.

## 2. Publicar o sistema completo no Railway

No Railway, escolha **New Project → Deploy from GitHub repo**, autorize `safe107/Safe-System` e selecione `main`. Mantenha a raiz do repositório como root directory. O arquivo `railway.toml` seleciona `infra/docker/hosted.Dockerfile`, que compila o front-end e a API.

Em **Settings → Networking → Generate Domain**, gere um domínio público e use a porta **10000** (ou a porta definida por `PORT`). O endereço real aparece no painel; não é possível presumir o nome antes da criação.

Em **Variables**, configure:

| Variável | Valor |
| --- | --- |
| `SAFE_PRODUCTION` | `true` |
| `COOKIE_SECURE` | `true` |
| `FRONTEND_URL` e `CORS_ALLOWED_ORIGINS` | A mesma URL HTTPS gerada para o serviço, sem barra final. |
| `DATABASE_URL` | URL JDBC com TLS validado descrita no passo 1. |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Credenciais do PostgreSQL. Referências Railway a `PGUSER` e `PGPASSWORD` podem ser usadas; a URL JDBC precisa ser montada separadamente. |
| `ADMIN_USERNAME` | Seu nome de acesso escolhido. |
| `ADMIN_PASSWORD` | Senha exclusiva com pelo menos 12 caracteres e no máximo 72 bytes UTF-8. |
| `STORAGE_MODE` | `s3` |
| `STORAGE_BUCKET` / `STORAGE_REGION` | Nome e região do bucket. |
| `STORAGE_ENDPOINT` | Vazio para AWS S3; endpoint HTTPS real para um provedor compatível. |
| `STORAGE_ACCESS_KEY` / `STORAGE_SECRET_KEY` | Credenciais restritas do bucket, somente no provedor. |

Salve as variáveis e faça deploy. A configuração inclui healthcheck `/actuator/health`. Site e API ficam na mesma origem: Caddy serve o site e encaminha `/api` para Java, que escuta apenas internamente. O Railway termina HTTPS. Não use `.local/dev.env` em produção.

Quando o serviço estiver saudável, abra seu domínio:

- `/pt-BR`: portfólio;
- `/admin/login`: painel;
- `/actuator/health`: deve responder `{"status":"UP"}`.

Entre com as credenciais escolhidas. Cadastre um projeto em rascunho, publique e confira a página pública. Envie um PDF de teste e confirme download, privacidade e ocultação. Só divulgue o link depois dessas verificações.

Depois do primeiro login, remova `ADMIN_PASSWORD` do ambiente e faça redeploy. O administrador está no banco; mudar a variável não redefine a senha. Mantenha a senha em um gerenciador. Configure backups e teste restauração. Sessões se perdem ao reiniciar a API.

## 3. Opcional: usar Vercel para o site

Este passo exige que o serviço Railway já esteja saudável. Importe `safe107/Safe-System` na Vercel, Root Directory **apps/web**, install **npm ci**, build **npm run build**, output **dist**.

O front-end deve chamar `/api` na mesma origem por proxy, para que os cookies de sessão funcionem entre provedores. Na raiz do seu checkout, execute com a **URL real** do Railway:

```bash
python3 infra/deployment/configure-vercel.py --api-url https://SEU-DOMINIO-REAL-DO-RAILWAY
```

Revise `apps/web/vercel.json`, versione e envie a alteração ao GitHub. O script configura a regra `/api/:path*` antes do fallback SPA. Não use um hostname de exemplo como destino e não adicione credenciais à URL.

Na Vercel, deixe `VITE_API_URL` vazio e defina `VITE_SITE_URL` como a URL real HTTPS da Vercel (ou seu domínio). No Railway, altere `FRONTEND_URL` e `CORS_ALLOWED_ORIGINS` para essa mesma origem da Vercel. Faça redeploy em ambos e valide login/CSRF/downloads a partir do endereço Vercel.

Sem configurar o proxy, o site estático sozinho não oferece o sistema completo. O roteiro básico do passo 2 dispensa essa etapa e já fornece um link único funcional. Prerender e sitemap são configurações adicionais descritas em DEPLOYMENT.md.
